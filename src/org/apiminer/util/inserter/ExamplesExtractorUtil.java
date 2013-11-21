package org.apiminer.util.inserter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.SystemProperties;
import org.apiminer.builder.IBuilder;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.api.ProjectAnalyserStatistic;
import org.apiminer.entities.api.Repository;
import org.apiminer.entities.example.Example;
import org.apiminer.extractor.ExampleExtractor;
import org.apiminer.util.BuilderUtil;
import org.apiminer.util.FilesUtil;
import org.apiminer.util.downloader.DownloaderFactory;
import org.apiminer.util.downloader.exceptions.DownloadException;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;

//TODO change it to a controller package
public class ExamplesExtractorUtil {
	
	private static final Logger LOGGER = Logger.getLogger(ExamplesExtractorUtil.class);
	
	private ExamplesExtractorUtil(){}
	
	public static void extractExamples(Project project, boolean isRetry) throws Exception {
		
		ProjectDAO projectDAO = new ProjectDAO();
		try {
			Project another = projectDAO.find(project.getName().trim(), DatabaseType.EXAMPLES);
			if (!isRetry) {
				if (another != null) {
					throw new IllegalArgumentException("Project already registred!");
				}
			}else{
				project = another;
			}
		} catch (Exception e1) {
			throw e1;
		}
		
		if (project.getClientOf() == null) {
			throw new IllegalArgumentException("Project is not a client!");
		}else if (project.getRepository() == null || project.getRepository().getRepositoryType() == null || project.getRepository().getUrlAddress() == null) {
			throw new IllegalArgumentException("Repository of project is not valid!");
		}

		Repository repository = project.getRepository();
		if(!isRetry){
		
			String localpath = null;
			
			LOGGER.debug("Downloading source files from repository");
			switch(repository.getRepositoryType()){
			
			case COMPRESSED:
				try {
					localpath = DownloaderFactory.getCompressedDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
				} catch (DownloadException e) {
					throw e;
				}
				break;
			
			case GIT:
				try {
					localpath = DownloaderFactory.getGitDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
				} catch (DownloadException e) {
					throw e;
				}
				break;
				
				
			case LOCAL:
				if (repository.getSourceFilesDirectory() == null) {
					localpath = repository.getUrlAddress();
				} else {
					localpath = repository.getSourceFilesDirectory();
				}
				break;
			
			case MERCURIAL:
				try {
					localpath = DownloaderFactory.getMercurialDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
				} catch (DownloadException e) {
					throw e;
				}
				break;
			
			case SUBVERSION:
				try {
					localpath = DownloaderFactory.getSubversionDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
				} catch (DownloadException e) {
					throw e;
				}
				break;
			
			default:
				break;
			
			}
			
			repository.setSourceFilesDirectory(localpath);
			repository.setJars(new HashSet<String>(FilesUtil.collectFiles(repository.getSourceFilesDirectory(), ".jar", true)));
		
		}
		
		LOGGER.debug("Building files using default builders");
		Set<Class<? extends IBuilder>> defaultBuilders = BuilderUtil.getBuilders();
		for (Class<? extends IBuilder> defaultBuilder : defaultBuilders) {
			try {
				IBuilder builder = defaultBuilder.newInstance();
				LOGGER.debug("Using builder '"+builder.getBuilderName()+"'");
				if (builder.build(repository.getSourceFilesDirectory())) {
					LOGGER.debug("Sucess on build with '"+builder.getBuilderName()+"'");
				} else {
					LOGGER.debug("Fail to build with '"+builder.getBuilderName()+"'");
				}
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		
		LOGGER.debug("Extracting the code examples");
		String sourceDirectory = project.getRepository().getSourceFilesDirectory();
		Set<String> jarsDependencies = project.getClientOf()
				.getRepository()
				.getJars();

		ExampleExtractor parser = new ExampleExtractor(
				sourceDirectory,
				jarsDependencies,
				project.getClientOf().getId());
		
		parser.parse();

		Set<ApiClass> apiClasses = parser.getApiClasses();
		Collection<Example> examples = parser.getExamples();
		
		LOGGER.debug("Removing similar examples");
		
		ExamplesExtractorUtil.removeSimilarExamples(examples);

		project.getRepository().setJars(new HashSet<String>(parser.getJarsDependencies()));
		project.setApiClass(apiClasses);
		project.setStatistics(new ProjectAnalyserStatistic()); 
		
		LOGGER.debug("Persisting client " + project.getName());
		
		ExampleDAO exampleDAO = new ExampleDAO(); 
		try {
			if (!isRetry) {
				exampleDAO.persist(project, examples);
			}else{
				exampleDAO.changeExamples(project.getId(), examples);
			}
			LOGGER.info("Client "+project+" processed and persisted");
		} catch (Exception e) {
			LOGGER.error(String.format("Error on persist the client %s, see the stack trace.", project.getName()));
			throw e;
		}
		
	}
	
	private static void removeSimilarExamples(final Collection<Example> examples){
		
		// Se a lista tem somente 1 exemplo, nao havera duplicacoes
		if (examples == null || examples.size() < 2) {
			return;
		}
		
		// Algoritmo para calculo da distância
		AbstractStringMetric metric = new CosineSimilarity();
		
		// Separo os exemplos por grupos, baseado nos elementos que sao baseados
		Map<Set<ApiMethod>, List<Example>> groups = new HashMap<Set<ApiMethod>, List<Example>>();
		for (Example ex : examples) {
			if (!groups.containsKey(ex.getApiMethods())) {
				groups.put(ex.getApiMethods(), new LinkedList<Example>());
			}
			groups.get(ex.getApiMethods()).add(ex);
		}
		
		// Limpa a lista recebida
		examples.clear();
		
		// Itera sobre os grupos buscando por exemplos duplicados
		for (Set<ApiMethod> group : groups.keySet()) {
			
			Example[] examplesGroupArray = groups.get(group).toArray(new Example[0]);
			
			// Mapeia os exemplos em strings. Neste passo exemplos completamente iguais ja sao removidos.
			LinkedHashMap<String, Example> examplesMap = new LinkedHashMap<String, Example>();
			for (Example ex : examplesGroupArray) {
				if (!examplesMap.containsKey(ex.getCodeExample())) {
					examplesMap.put(ex.getCodeExample(), ex);
				}
			}
			
			// Remove exemplos semelhantes
			while(!examplesMap.isEmpty()) {

				String[] examplesStringArray = examplesMap.keySet().toArray(new String[0]);
				Example example = examplesMap.remove(examplesStringArray[0]);
				examples.add(example);
				
				float[] result = metric.batchCompareSet(examplesStringArray, example.getCodeExample());
				for (int j = 1; j < result.length; j++) {
					if (result[j] >= 0.8) {
						examplesMap.remove(examplesStringArray[j]);
					}
				}
			}
			
		}
		
	}
	
}
