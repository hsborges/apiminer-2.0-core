package org.apiminer.entities.api;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apiminer.entities.ProjectAnalyserStatistic;
import org.eclipse.persistence.annotations.ReplicationPartitioning;

@SuppressWarnings("serial")
@Entity
@Table(name = "Project", uniqueConstraints=@UniqueConstraint(columnNames = "name"))
@ReplicationPartitioning(name = "Replicate")
public class Project implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", columnDefinition = "varchar(254)", nullable = false)
	private String name;

	@Column(name = "summary", columnDefinition = "text", nullable = true)
	private String summary;

	@Column(name = "url_site", columnDefinition = "varchar(254)", nullable = true)
	private String urlSite;

	@OneToOne(optional = false, cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "repository_id")
	private Repository repository;

	@OneToOne(optional = false, cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "statistics_id")
	private ProjectAnalyserStatistic statistics;

	@ManyToOne(optional = true, cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "client_of_id")
	private Project clientOf;

	@OneToMany(mappedBy = "project", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@OrderBy("name")
	private Set<ApiClass> apiClass = new HashSet<ApiClass>();

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at", columnDefinition = "timestamp", nullable = false)
	private Date addedAt;
	
	@Column(name = "project_status", columnDefinition = "text", nullable = false)
	private String projectStatus;
	
	public Project() {
		super();
		this.apiClass = new HashSet<ApiClass>();
		this.addedAt = new Date(System.currentTimeMillis());
		this.statistics = new ProjectAnalyserStatistic();
		this.projectStatus = ProjectStatus.UNKNOWN.name();
	}

	public Project(String name, Repository repository) {
		this();
		this.name = name;
		this.repository = repository;
	}

	public Project(String name, Repository repository, Project clientOf) {
		this(name, repository);
		this.name = name;
		this.repository = repository;
		this.clientOf = clientOf;
	}

	@Transient
	public boolean isClient() {
		if (this.clientOf != null) {
			return true;
		} else {
			return false;
		}
	}

	@Transient
	public boolean isSource() {
		if (this.clientOf == null) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append("Project [id=")
				.append(id)
				.append(", name=")
				.append(name)
				.append("]");
		return builder.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getUrlSite() {
		return urlSite;
	}

	public void setUrlSite(String urlSite) {
		this.urlSite = urlSite;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public ProjectAnalyserStatistic getStatistics() {
		return statistics;
	}

	public void setStatistics(ProjectAnalyserStatistic statistics) {
		this.statistics = statistics;
	}

	public Project getClientOf() {
		return clientOf;
	}

	public void setClientOf(Project clientOf) {
		this.clientOf = clientOf;
	}

	public Set<ApiClass> getApiClass() {
		return apiClass;
	}

	public void setApiClass(Set<ApiClass> apiClass) {
		this.apiClass = apiClass;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Project))
			return false;
		Project other = (Project) obj;
		if (clientOf == null) {
			if (other.clientOf != null)
				return false;
		} else if (!clientOf.equals(other.clientOf))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(ProjectStatus projectStatus) {
		this.projectStatus = projectStatus.name();
	}

}