package org.apiminer.tasks.implementations;

import java.io.File;
import java.util.Date;
import java.util.HashSet;

import org.apiminer.SystemProperties;
import org.apiminer.analyzer.AnalyzerException;
import org.apiminer.analyzer.api.APIAnalyzer;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.api.ProjectStatus;
import org.apiminer.entities.api.Repository;
import org.apiminer.entities.api.RepositoryType;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.util.FilesUtil;
import org.apiminer.util.downloader.DownloaderFactory;


/**
 * Task to analyze and insert an API in the configured database. 
 * 
 * @author Hudson S. Borges
 *
 */
public class APIAnalyzerTask extends AbstractTask {

	private Project project;

	private boolean includePublic;
	private boolean includePrivate;
	private boolean includeProtected;

	protected APIAnalyzerTask() {
		super();
		this.project = new Project();
		this.project.setAddedAt(new Date());
		this.project.setClientOf(null);
	}

	public APIAnalyzerTask(String name,
			String summary,
			String url,
			ProjectStatus projectStatus,
			RepositoryType repositoryType,
			String urlRepository,
			boolean includePublic,
			boolean includePrivate, 
			boolean includeProtected) {
		
		this();
		
		this.project.setName(name);
		this.project.setSummary(summary);
		this.project.setUrlSite(url);
		this.project.setProjectStatus(projectStatus);
		
		Repository repository = new Repository();
		repository.setRepositoryType(repositoryType);
		repository.setUrlAddress(urlRepository);
		
		this.project.setRepository(repository);
		
		this.includePublic = includePublic;
		this.includePrivate = includePrivate;
		this.includeProtected = includeProtected;
	}

	@Override
	public void execute() {
		super.setStatus(TaskStatus.RUNNING);
		
		try {
			if (new ProjectDAO().findSourceAPI() != null) {
				throw new AnalyzerException("There is already a registered API");
			}
			
			File localPathFile = null;
			Repository repository = this.project.getRepository();
			
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
			repository.setJars(new HashSet<String>(FilesUtil.collectFiles(repository.getSourceFilesDirectory(), ".jar", true)));
			repository.setProject(project);

			APIAnalyzer jdtParser = new APIAnalyzer(
					repository.getSourceFilesDirectory(), 
					repository.getJars().toArray(new String[0]), 
					includePublic,
					includePrivate, 
					includeProtected);
			
			jdtParser.parse();
			
			project.getRepository().setJars(new HashSet<String>(jdtParser.getJarsDependencies()) );
			project.setApiClass(jdtParser.getApiClasses());
			project.setStatistics(jdtParser.getStatistics());

			new ProjectDAO().persist(project, DatabaseType.REPLICATED);

			super.setResult(TaskResult.SUCCESS);
		} catch (Throwable throwable) {
			super.setResult(throwable);
		} finally {
			super.setStatus(TaskStatus.FINISHED);
		}
		
	}

	@Override
	public String toString() {
		return project.getName()+" API analyze.";
	}

}
