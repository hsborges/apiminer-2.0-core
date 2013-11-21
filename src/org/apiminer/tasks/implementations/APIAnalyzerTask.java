package org.apiminer.tasks.implementations;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.apiminer.analyzer.api.APIAnalyzer;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.Project;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;


public class APIAnalyzerTask extends AbstractTask {

	private final Logger logger = Logger.getLogger(APIAnalyzerTask.class);

	private Project project;

	private boolean includePublic;
	private boolean includePrivate;
	private boolean includeProtected;

	private APIAnalyzerTask() {
		super();
		super.status = TaskStatus.WAITING;
		notifyObservers(TaskStatus.WAITING);
	}

	public APIAnalyzerTask(Project project, boolean includePublic,
			boolean includePrivate, boolean includeProtected) {
		this();
		this.project = project;
		this.includePublic = includePublic;
		this.includePrivate = includePrivate;
		this.includeProtected = includeProtected;
	}

	@Override
	public void execute() {
		super.status = TaskStatus.RUNNING;
		notifyObservers(TaskStatus.RUNNING);

		ProjectDAO projectDAO = new ProjectDAO();
		try {

			logger.info(String.format("The analyze of the API %s was started.", project.getName()));

			String sourceFilesDirectory = project.getRepository().getSourceFilesDirectory();
			String[] jarsDependencies = project.getRepository().getJars().toArray(new String[0]);

			APIAnalyzer jdtParser = new APIAnalyzer(
					sourceFilesDirectory, jarsDependencies, includePublic,
					includePrivate, includeProtected);
			jdtParser.parse();
			
			project.getRepository().setJars(new HashSet<String>(jdtParser.getJarsDependencies()) );
			project.setApiClass(jdtParser.getApiClasses());
			project.setStatistics(jdtParser.getStatistics());

			logger.debug(String.format("Persisting %s API data.", project.getName()));

			projectDAO.persist(project, DatabaseType.PRE_PROCESSING);

			logger.info("API "+project.getName()+" analyzed.");

			super.result = TaskResult.SUCCESS;
		} catch (Throwable throwable) {
			logger.error(throwable.getLocalizedMessage(), throwable);
			super.result = TaskResult.FAILURE;
			super.result.setProblem(throwable);
		} finally {
			super.status = TaskStatus.FINISHED;
			notifyObservers(TaskStatus.FINISHED);
		}
		
	}

	@Override
	public String toString() {
		return project.getName()+" API analyze.";
	}

}
