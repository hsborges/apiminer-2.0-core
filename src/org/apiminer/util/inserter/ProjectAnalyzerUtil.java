package org.apiminer.util.inserter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.SystemProperties;
import org.apiminer.analyzer.AnalyzerException;
import org.apiminer.analyzer.client.ClientAnalyzer;
import org.apiminer.builder.IBuilder;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.api.Repository;
import org.apiminer.entities.api.RepositoryType;
import org.apiminer.util.BuilderUtil;
import org.apiminer.util.FilesUtil;
import org.apiminer.util.downloader.DownloaderFactory;
import org.apiminer.util.downloader.exceptions.DownloadException;

/**
 * 		Classe utilitária que provê os procedimentos necessários para inserção de sistemas na base de dados.
 * 		Tipos de sistemas que podem ser inseridos com esta classe:
 * 
 * 		<ul>
 * 			<li>API fonte (Que deve ser único em uma base de dados); e </li>
 * 			<li>Sistemas Clientes (Sistemas que utilizam da API desejada)</li>
 * 		</ul>
 * 
 * 		Procedimentos realizados:
 * 
 * 		<ul>
 * 			<li>Verifica se as informações do projeto são válidas; </li>
 * 			<li>Verifica se o projeto já foi cadastrado anteriormente; </li>
 * 			<li>Obtem o projeto de repositórios online, se preciso;</li>
 * 			<li>Executa contruções previamente cadastradas;</li>
 * 			<li>Analisa o projeto; e </li>
 * 			<li>Persiste as informações na base de dados. </li>
 * 		</ul>
 *   
 * 
 * @author Hudson S. Borges
 *
 */
//TODO change it to a controller package
public class ProjectAnalyzerUtil {
	
	private static final String DOWNLOADING_SOURCE_FILES = "Downloading source files.";
	
	private static final Logger LOGGER = Logger.getLogger(ProjectAnalyzerUtil.class);
	
	private ProjectAnalyzerUtil(){}
	
	/**
	 * 	Processa e insere o projetos clientes na base de dados.
	 * 
	 * @param project Projeto a ser processado
	 * @throws DownloadException Caso haja problemas durante o download do sistema
	 * @throws AnalyzerException Caso haja problemas durante a análise do código fonte do sistema
	 */
	public static void analyzeClient(Project project) throws DownloadException, AnalyzerException {
		
		ProjectDAO projectDAO = new ProjectDAO();
		
		if (project.getClientOf() == null) {
			throw new IllegalArgumentException("The must be a client!");
		}else if (project.getRepository() == null || project.getRepository().getRepositoryType() == null || project.getRepository().getUrlAddress() == null) {
			throw new IllegalArgumentException("Invalid repository!");
		}else {
			if (projectDAO.find(project.getName().trim(), DatabaseType.PRE_PROCESSING) != null){
				throw new IllegalArgumentException("Project name already registered!");
			}
		}
		
		String localpath = null;
		Repository repository = project.getRepository();
		
		switch(repository.getRepositoryType()){
		
		case COMPRESSED:
			try {	
				LOGGER.debug(DOWNLOADING_SOURCE_FILES);
				localpath = DownloaderFactory.getCompressedDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
			} catch (DownloadException e) {
				throw e;
			}
			break;
		
		case GIT:
			try {
				LOGGER.debug(DOWNLOADING_SOURCE_FILES);
				localpath = DownloaderFactory.getGitDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
			} catch (DownloadException e) {
				throw e;
			}
			break;
			
			
		case LOCAL:
			localpath = repository.getUrlAddress();
			break;
		
		case MERCURIAL:
			try {
				LOGGER.debug(DOWNLOADING_SOURCE_FILES);
				localpath = DownloaderFactory.getMercurialDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
			} catch (DownloadException e) {
				throw e;
			}
			break;
		
		case SUBVERSION:
			try {
				LOGGER.debug(DOWNLOADING_SOURCE_FILES);
				localpath = DownloaderFactory.getSubversionDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath()).getAbsolutePath();
			} catch (DownloadException e) {
				throw e;
			}
			break;
		
		default:
			break;
		
		}
		
		repository.setSourceFilesDirectory(localpath);
		Collection<String> jars = FilesUtil.collectFiles(repository.getSourceFilesDirectory(), ".jar", true);
		if (jars != null) {
			repository.setJars(new HashSet<String>(jars));
		} else { 
			repository.setJars(new HashSet<String>());
		}
		
		try {
			
			LOGGER.debug("Building files using the defaults builders.");
			Set<Class<? extends IBuilder>> defaultBuilders = BuilderUtil.getBuilders();
			for (Class<? extends IBuilder> defaultBuilder : defaultBuilders) {
				try {
					IBuilder builderInstance = (IBuilder) defaultBuilder.newInstance();
					LOGGER.debug("Building with '"+builderInstance.getBuilderName()+"'");
					if (builderInstance.build(localpath)) {
						LOGGER.debug(builderInstance.getBuilderName()+" worked!");
					} else {
						LOGGER.debug(builderInstance.getBuilderName()+" not worked!");
					}
				} catch (Exception e) {
					LOGGER.error(e);
				}
			}
			
			LOGGER.debug("Processing the client " + project.getName());

			String sourceDirectory = project.getRepository().getSourceFilesDirectory();
			String[] jarsDependencies = project.getClientOf()
					.getRepository()
					.getJars()
					.toArray(new String[0]);

			ClientAnalyzer parser = new ClientAnalyzer(
					sourceDirectory,
					jarsDependencies,
					project.getClientOf());
			parser.parse();

			Set<ApiClass> apiClasses = parser.getApiClasses();

			project.getRepository().setJars(new HashSet<String>(parser.getJarsDependency()));
			project.setApiClass(apiClasses);
			project.setStatistics(parser.getStatistics());
			
			projectDAO.persist(project, DatabaseType.PRE_PROCESSING);
		
		} catch (IOException e) {
			//TODO Tratar casos em que ocorram problemas quando buscar os construtores
		} catch (AnalyzerException e) {
			throw e;
		} finally {
			if (repository.getRepositoryType() != RepositoryType.LOCAL){ 
				try {
					FilesUtil.deleteFile(new File(localpath));
				} catch (IOException e) {
					//TODO Tratar problemas ao apagar diretório temporário
				}
			}
		}
		
		LOGGER.debug("Client "+project+" analyzed and persisted.");
	}

}
