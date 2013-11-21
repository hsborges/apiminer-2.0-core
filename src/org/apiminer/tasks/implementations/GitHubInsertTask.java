package org.apiminer.tasks.implementations;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.GitHubRepository;
import org.apiminer.entities.api.Project;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.util.inserter.ProjectAnalyzerUtil;


public class GitHubInsertTask extends AbstractTask {

	private static final Logger LOGGER = Logger
			.getLogger(GitHubInsertTask.class);

	private Project project;

	public GitHubInsertTask(GitHubRepository repository, long apiSourceId) {

		ProjectDAO projectDAO = new ProjectDAO();
		try {
			this.project = new Project();
			this.project.setName(String.format("%s%c%s", repository.getOwner(),'.', repository.getName()));
			if (projectDAO.find(this.project.getName(), DatabaseType.PRE_PROCESSING) != null) {
				throw new IllegalArgumentException(String.format("Project %s already registred", this.project.getName()));
			}
			
			Project sourceProject = projectDAO.find(apiSourceId, DatabaseType.PRE_PROCESSING);
			if (sourceProject == null) {
				throw new IllegalArgumentException("Source project not exist");
			}
			this.project.setClientOf(sourceProject);
			
			this.project.setAddedAt(new Date(System.currentTimeMillis()));
			this.project.setRepository(repository);
			this.project.setSummary(repository.getDescription());
			this.project.setUrlSite("https://github.com/"
					+ repository.getOwner() + "/" + repository.getName());

			repository.setUrlAddress("git://github.com/"
					+ repository.getOwner() + "/" + repository.getName()
					+ ".git");

		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

		super.status = TaskStatus.WAITING;
		notifyObservers("Waiting");

	}

	@Override
	public void execute() {

		super.status = TaskStatus.RUNNING;
		notifyObservers("working");

		try {
			ProjectAnalyzerUtil.analyzeClient(this.project);
			super.status = TaskStatus.FINISHED;
			super.result = TaskResult.SUCCESS;
			notifyObservers(super.result);
		} catch (Throwable e) {
			LOGGER.error("Error on process project " + this.project.getName(), e);
			super.status = TaskStatus.FINISHED;
			super.result = TaskResult.FAILURE;
			super.result.setProblem(e);
			notifyObservers(super.result);
		} 

	}

	@Override
	public String toString() {
		return "Insert github project " + project.getName();
	}

}
