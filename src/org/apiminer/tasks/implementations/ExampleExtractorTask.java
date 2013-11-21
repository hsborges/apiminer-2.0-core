package org.apiminer.tasks.implementations;

import org.apiminer.entities.api.Project;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.util.inserter.ExamplesExtractorUtil;


public class ExampleExtractorTask extends AbstractTask {

	private Project project;

	private ExampleExtractorTask() {
		super();
		super.status = TaskStatus.WAITING;
		notifyObservers(super.status.toString());
	}

	public ExampleExtractorTask(Project project) {
		this();

		if (project.getClientOf() == null) {
			throw new IllegalArgumentException("The API must be not null");
		}

		this.project = project;
	}

	@Override
	public void execute() {
		super.status = TaskStatus.RUNNING;
		super.notifyObservers(TaskStatus.RUNNING);
		
		try {
			ExamplesExtractorUtil.extractExamples(project, false);
			super.result = TaskResult.SUCCESS;
		} catch (Throwable throwable) {
			super.result = TaskResult.FAILURE;
			super.result.setProblem(throwable);
		} finally {
			super.status = TaskStatus.FINISHED;
			super.notifyObservers(TaskStatus.FINISHED);
		}
		
	}

	@Override
	public String toString() {
		return String.format("Extraction of code examples from client %s", project.getName());
	}

}
