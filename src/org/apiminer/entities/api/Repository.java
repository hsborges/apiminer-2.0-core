package org.apiminer.entities.api;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "Repository")
public class Repository implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "source_files_directory", columnDefinition = "varchar(254)", nullable = false)
	private String sourceFilesDirectory;

	@ElementCollection
	@CollectionTable(name = "JarsDependencies", joinColumns=@JoinColumn(name = "repository_id"))
	private Set<String> jars;

	@Column(name = "url_address", nullable = false)
	private String urlAddress;

	@Column(name = "repository_type", nullable = false)
	private String repositoryType;

	@OneToOne(mappedBy = "repository", cascade = { CascadeType.REFRESH })
	private Project project;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the sourceFilesDirectory
	 */
	public String getSourceFilesDirectory() {
		return sourceFilesDirectory;
	}

	/**
	 * @param sourceFilesDirectory
	 *            the sourceFilesDirectory to set
	 */
	public void setSourceFilesDirectory(String sourceFilesDirectory) {
		this.sourceFilesDirectory = sourceFilesDirectory;
	}

	/**
	 * @return the urladdress
	 */
	public String getUrlAddress() {
		return urlAddress;
	}

	/**
	 * @param urladdress
	 *            the urladdress to set
	 */
	public void setUrlAddress(String urladdress) {
		this.urlAddress = urladdress;
	}

	/**
	 * @return the repositoryType
	 */
	public RepositoryType getRepositoryType() {
		return RepositoryType.parse(this.repositoryType);
	}

	/**
	 * @param repositoryType
	 *            the repositoryType to set
	 */
	public void setRepositoryType(RepositoryType repositorytype) {
		this.repositoryType = repositorytype.toString();
	}

	/**
	 * @return the project
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
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
		if (!(obj instanceof Repository))
			return false;
		Repository other = (Repository) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (repositoryType == null) {
			if (other.repositoryType != null)
				return false;
		} else if (!repositoryType.equals(other.repositoryType))
			return false;
		return true;
	}

	public Set<String> getJars() {
		return jars;
	}

	public void setJars(Set<String> jars) {
		this.jars = jars;
	}
	
}