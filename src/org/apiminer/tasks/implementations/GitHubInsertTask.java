package org.apiminer.tasks.implementations;

import org.apiminer.entities.api.GitHubRepository;


public class GitHubInsertTask extends ClientAnalyzerTask {

	public GitHubInsertTask(GitHubRepository repository) {
		super();
		
		this.project.setName(String.format("%s%c%s", repository.getOwner(),'.', repository.getName()));
		this.project.setRepository(repository);
		this.project.setSummary(repository.getDescription());
		this.project.setUrlSite(String.format("https://github.com/%s/%s", repository.getOwner(), repository.getName()));

		repository.setUrlAddress(String.format("git://github.com/%s/%s.git", repository.getOwner(), repository.getName()));
	}

	@Override
	public String toString() {
		return "Insert github project " + project.getName();
	}

}
