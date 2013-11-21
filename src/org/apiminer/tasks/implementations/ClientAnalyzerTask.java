package org.apiminer.tasks.implementations;

import org.apache.log4j.Logger;
import org.apiminer.entities.api.Project;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.util.inserter.ProjectAnalyzerUtil;


public class ClientAnalyzerTask extends AbstractTask {

	private final Logger logger = Logger.getLogger(ClientAnalyzerTask.class);

	private Project project;

	private ClientAnalyzerTask() {
		super();
		super.status = TaskStatus.WAITING;
	}

	public ClientAnalyzerTask(Project project) {
		this();

		if (project.getClientOf() == null) {
			throw new IllegalArgumentException("The API of the client must be not null!");
		}

		this.project = project;
	}

	@Override
	public void execute() {
		super.status = TaskStatus.RUNNING;
		notifyObservers(TaskStatus.RUNNING);
		
		logger.info(String.format("The analyze of the client %s was started.", project.getName()));

		try {
			ProjectAnalyzerUtil.analyzeClient(project);
			super.result = TaskResult.SUCCESS;
		} catch (Throwable throwable) {
			logger.error(throwable);
			super.result = TaskResult.FAILURE;
			super.result.setProblem(throwable);
			notifyObservers(super.result);
		} finally {
			super.status = TaskStatus.FINISHED;
			notifyObservers(TaskStatus.FINISHED);
		}
		
		logger.info(String.format("Client %s analyzed!", project.getName()));
	}

	@Override
	public String toString() {
		return project.getName()+" analyze.";
	}

}
