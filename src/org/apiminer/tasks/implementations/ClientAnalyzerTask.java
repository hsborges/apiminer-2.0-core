package org.apiminer.tasks.implementations;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apiminer.SystemProperties;
import org.apiminer.analyzer.AnalyzerException;
import org.apiminer.analyzer.client.ClientAnalyzer;
import org.apiminer.builder.IBuilder;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.api.ProjectStatus;
import org.apiminer.entities.api.Repository;
import org.apiminer.entities.api.RepositoryType;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.util.BuilderUtil;
import org.apiminer.util.FilesUtil;
import org.apiminer.util.downloader.DownloaderFactory;


/**
 * Task to analyze and insert an API client in the configured database. 
 * 
 * @author Hudson S. Borges
 *
 */
public class ClientAnalyzerTask extends AbstractTask {

	protected Project project;

	protected ClientAnalyzerTask() {
		super();
		super.setStatus(TaskStatus.WAITING);
		
		this.project = new Project();
		this.project.setAddedAt(new Date());
		this.project.setClientOf(new ProjectDAO().findSourceAPI());
		this.project.setProjectStatus(ProjectStatus.UNKNOWN);
	}

	public ClientAnalyzerTask(String name,
			String summary,
			String url,
			ProjectStatus projectStatus,
			RepositoryType repositoryType,
			String urlRepository){
		
		this();
		
		this.project.setName(name);
		this.project.setSummary(summary);
		this.project.setUrlSite(url);
		this.project.setProjectStatus(ProjectStatus.UNKNOWN);
		
		Repository repository = new Repository();
		repository.setRepositoryType(repositoryType);
		repository.setUrlAddress(urlRepository);
		
		this.project.setRepository(repository);
	}

	@Override
	public void execute() {
		super.setStatus(TaskStatus.RUNNING);
		
		try {
			if (new ProjectDAO().find(project.getName().trim(), DatabaseType.PRE_PROCESSING) != null){
				throw new AnalyzerException("Project name already registered!");
			}
		
			File localPathFile = null;
			Repository repository = project.getRepository();
			
			switch(repository.getRepositoryType()){
			
			case COMPRESSED:
				localPathFile = DownloaderFactory.getCompressedDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath());
				break;
			
			case GIT:
				localPathFile = DownloaderFactory.getGitDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath());
				break;
				
				
			case LOCAL:
				localPathFile = new File(repository.getUrlAddress());
				break;
			
			case MERCURIAL:
				localPathFile = DownloaderFactory.getMercurialDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath());
				break;
			
			case SUBVERSION:
				localPathFile = DownloaderFactory.getSubversionDownloader().download(project.getName(), repository.getUrlAddress(), SystemProperties.WORKING_DIRECTORY.getAbsolutePath());
				break;
			
			default:
				break;
			
			}
			
			repository.setSourceFilesDirectory(localPathFile.getAbsolutePath());
			Collection<String> jars = FilesUtil.collectFiles(repository.getSourceFilesDirectory(), ".jar", true);
			if (jars != null) {
				repository.setJars(new HashSet<String>(jars));
			} else { 
				repository.setJars(new HashSet<String>());
			}
			
			try {
				
//				LOGGER.debug("Building files using the defaults builders.");
				Set<Class<? extends IBuilder>> defaultBuilders = BuilderUtil.getBuilders();
				for (Class<? extends IBuilder> defaultBuilder : defaultBuilders) {
					try {
						IBuilder builderInstance = (IBuilder) defaultBuilder.newInstance();
//						LOGGER.debug("Building with '"+builderInstance.getBuilderName()+"'");
						if (builderInstance.build(localPathFile.getAbsolutePath())) {
//							LOGGER.debug(builderInstance.getBuilderName()+" worked!");
						} else {
//							LOGGER.debug(builderInstance.getBuilderName()+" not worked!");
						}
					} catch (Exception e) {
//						LOGGER.error(e);
					}
				}
				
//				LOGGER.debug("Processing the client " + project.getName());

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
				
				new ProjectDAO().persist(project, DatabaseType.PRE_PROCESSING);
			} catch (IOException e) {
				//TODO Tratar casos em que ocorram problemas quando buscar os construtores
			} catch (AnalyzerException e) {
				throw e;
			} finally {
				if (repository.getRepositoryType() != RepositoryType.LOCAL){ 
					try {
						FilesUtil.deleteFile(localPathFile);
					} catch (IOException e) {
						//TODO Tratar problemas ao apagar diretório temporário
						e.printStackTrace();
					}
				}
			}
			
			super.setResult(TaskResult.SUCCESS);
		} catch (Throwable throwable) {
			super.setResult(throwable);
		} finally {
			super.setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public String toString() {
		return project.getName()+" analyze.";
	}

}
