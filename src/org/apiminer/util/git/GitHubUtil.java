package org.apiminer.util.git;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apiminer.entities.api.GitHubRepository;
import org.apiminer.entities.api.RepositoryType;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class GitHubUtil {
	
	private static final String CLONE_URL_MODEL = new String("https://github.com/%s/%s.git");
	private static final String GIT_URL_MODEL = new String("git://github.com/%s/%s.git");
	private static final String HTML_URL_MODEL = new String("https://github.com/%s/%s");
	private static final String SSH_URL_MODEL = new String("git@github.com:%s/%s");
	private static final String SVN_URL_MODEL = new String("https://svn.github.com/%s/%s");

	public static final String JAVA_LANGUAGE = "Java";

	private String username;
	private String password;

	/**
	 *  Create a new object to search as anonymous user
	 */
	public GitHubUtil() {
		super();
	}

	/**
	 *  Create a new object to search logged
	 *  
	 *  @param username github username
	 *  @param password github password
	 */
	public GitHubUtil(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Search for repositories with the keyword.
	 * 
	 * @param keyword 
	 * @param startPage
	 * @param endPage
	 * @return
	 * @throws IOException
	 */
	public List<GitHubRepository> searchRepositories(final String keyword, int startPage, int endPage) throws IOException {
		GitHubClient client = new GitHubClient();
		client.setCredentials(username, password);
		
		RepositoryService repositoryService = new RepositoryService(client);
		CommitService commitService = new CommitService(client);

		List<SearchRepository> searchRepositories = new LinkedList<SearchRepository>();
		List<GitHubRepository> repositories = new LinkedList<GitHubRepository>();

		int initPage = startPage;
		do {
			searchRepositories = repositoryService.searchRepositories(keyword, JAVA_LANGUAGE, initPage++);

			for (SearchRepository searchRepository : searchRepositories) {
				GitHubRepository repository = new GitHubRepository();
				
				repository.setCloneUrl(String.format(CLONE_URL_MODEL, searchRepository.getOwner(), searchRepository.getName()));
				
				try {
					repository.setCommits(commitService.getCommits(searchRepository).size());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				repository.setCreatedAt(searchRepository.getCreatedAt());
				repository.setDescription(searchRepository.getDescription());
				repository.setFork(searchRepository.isFork());
				repository.setForks(searchRepository.getForks());
				repository.setGitUrl(String.format(GIT_URL_MODEL, searchRepository.getOwner(), searchRepository.getName()));
				repository.setHasDownloads(searchRepository.isHasDownloads());
				repository.setHasIssues(searchRepository.isHasIssues());
				repository.setHasWiki(searchRepository.isHasWiki());
				repository.setHomepage(searchRepository.getHomepage());
				repository.setHtmlUrl(String.format(HTML_URL_MODEL, searchRepository.getOwner(), searchRepository.getName()));
				repository.setLanguage(searchRepository.getLanguage());
				repository.setMasterBranch("master");
				repository.setMirrorUrl(null);
				repository.setName(searchRepository.getName());
				repository.setOpenIssues(searchRepository.getOpenIssues());
				repository.setOwner(searchRepository.getOwner());
				repository.setPrivate(searchRepository.isPrivate());
				repository.setPushedAt(searchRepository.getPushedAt());
				repository.setSize(searchRepository.getSize());
				repository.setSshUrl(String.format(SSH_URL_MODEL, searchRepository.getOwner(), searchRepository.getName()));
				repository.setSvnUrl(String.format(SVN_URL_MODEL, searchRepository.getOwner(), searchRepository.getName()));
				repository.setUpdatedAt(null);
				repository.setUrl(searchRepository.getUrl());
				repository.setUrlAddress(searchRepository.getUrl());
				repository.setWatchers(searchRepository.getWatchers());

				repository.setRepositoryType(RepositoryType.GIT);
				
				repositories.add(repository);
			}

		} while (searchRepositories.size() > 0 && initPage <= endPage);

		GitHubUtil.removeInvalidsProjects(repositories);
		
		return repositories;
	}

	private static void removeInvalidsProjects(List<GitHubRepository> gitHubRepositories){
		Map<String, GitHubRepository> repositoriesMap = new HashMap<String, GitHubRepository>();
		for (GitHubRepository ghr : gitHubRepositories) {
			if (!ghr.getLanguage().trim().equalsIgnoreCase(JAVA_LANGUAGE)) {
				continue;
			}
			
			String name = ghr.getName();
			GitHubRepository anotherRepository = repositoriesMap.get(name);  
			if (anotherRepository != null){
				if (ghr.getCreatedAt().before(anotherRepository.getCreatedAt())) {
					repositoriesMap.put(name, ghr);
				}
			}else{
				repositoriesMap.put(name, ghr);
			}
			
		}
		
		gitHubRepositories.clear();
		gitHubRepositories.addAll(repositoriesMap.values());
	}

}
