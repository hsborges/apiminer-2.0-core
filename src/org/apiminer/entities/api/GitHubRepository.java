package org.apiminer.entities.api;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("serial")
@Entity
public class GitHubRepository extends Repository {

	@Column(name = "fork", columnDefinition = "boolean")
	private boolean fork;

	@Column(name = "has_downloads", columnDefinition = "boolean")
	private boolean hasDownloads;

	@Column(name = "has_issues", columnDefinition = "boolean")
	private boolean hasIssues;

	@Column(name = "has_wiki", columnDefinition = "boolean")
	private boolean hasWiki;

	@Column(name = "is_private", columnDefinition = "boolean")
	private boolean isPrivate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_at", columnDefinition = "timestamp")
	private Date createdAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "pushed_at", columnDefinition = "timestamp")
	private Date pushedAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at", columnDefinition = "timestamp")
	private Date updatedAt;

	@Column(name = "commits", columnDefinition = "integer")
	private int commits;
	
	@Column(name = "forks", columnDefinition = "integer")
	private int forks;

	@Column(name = "open_issues", columnDefinition = "integer")
	private int openIssues;

	@Column(name = "size", columnDefinition = "integer")
	private int size;

	@Column(name = "watchers", columnDefinition = "integer")
	private int watchers;

	@Column(name = "clone_url", columnDefinition = "varchar(254)")
	private String cloneUrl;

	@Column(name = "description", columnDefinition = "text")
	private String description;

	@Column(name = "homepage", columnDefinition = "varchar(254)")
	private String homepage;

	@Column(name = "git_url", columnDefinition = "varchar(254)")
	private String gitUrl;

	@Column(name = "html_url", columnDefinition = "varchar(254)")
	private String htmlUrl;

	@Column(name = "language", columnDefinition = "varchar(254)")
	private String language;

	@Column(name = "master_branch", columnDefinition = "varchar(254)")
	private String masterBranch;

	@Column(name = "mirror_url", columnDefinition = "varchar(254)")
	private String mirrorUrl;

	@Column(name = "name", columnDefinition = "varchar(254)")
	private String name;

	@Column(name = "ssh_url", columnDefinition = "varchar(254)")
	private String sshUrl;

	@Column(name = "svn_url", columnDefinition = "varchar(254)")
	private String svnUrl;

	@Column(name = "url", columnDefinition = "varchar(254)")
	private String url;

	@Column(name = "owner", columnDefinition = "varchar(254)")
	private String owner;

	public boolean isFork() {
		return fork;
	}

	public void setFork(boolean fork) {
		this.fork = fork;
	}

	public boolean isHasDownloads() {
		return hasDownloads;
	}

	public void setHasDownloads(boolean hasDownloads) {
		this.hasDownloads = hasDownloads;
	}

	public boolean isHasIssues() {
		return hasIssues;
	}

	public void setHasIssues(boolean hasIssues) {
		this.hasIssues = hasIssues;
	}

	public boolean isHasWiki() {
		return hasWiki;
	}

	public void setHasWiki(boolean hasWiki) {
		this.hasWiki = hasWiki;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getPushedAt() {
		return pushedAt;
	}

	public void setPushedAt(Date pushedAt) {
		this.pushedAt = pushedAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	public int getOpenIssues() {
		return openIssues;
	}

	public void setOpenIssues(int openIssues) {
		this.openIssues = openIssues;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getWatchers() {
		return watchers;
	}

	public void setWatchers(int watchers) {
		this.watchers = watchers;
	}

	public String getCloneUrl() {
		return cloneUrl;
	}

	public void setCloneUrl(String cloneUrl) {
		this.cloneUrl = cloneUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getGitUrl() {
		return gitUrl;
	}

	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	public String getHtmlUrl() {
		return htmlUrl;
	}

	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMasterBranch() {
		return masterBranch;
	}

	public void setMasterBranch(String masterBranch) {
		this.masterBranch = masterBranch;
	}

	public String getMirrorUrl() {
		return mirrorUrl;
	}

	public void setMirrorUrl(String mirrorUrl) {
		this.mirrorUrl = mirrorUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSshUrl() {
		return sshUrl;
	}

	public void setSshUrl(String sshUrl) {
		this.sshUrl = sshUrl;
	}

	public String getSvnUrl() {
		return svnUrl;
	}

	public void setSvnUrl(String svnUrl) {
		this.svnUrl = svnUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getCommits() {
		return commits;
	}

	public void setCommits(int commits) {
		this.commits = commits;
	}

}
