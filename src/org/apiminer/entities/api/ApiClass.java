package org.apiminer.entities.api;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@SuppressWarnings("serial")
@Entity
public class ApiClass extends ApiElement {

	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "project_id")
	private Project project;

	@Column(name = "is_interface", columnDefinition = "boolean")
	private boolean isInterface;
	
	@Column(name = "is_enum", columnDefinition = "boolean")
	private boolean isEnum;

	@OneToMany(mappedBy = "apiClass", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@OrderBy("name")
	private Set<ApiMethod> apiMethods = new HashSet<ApiMethod>();
	
	public ApiClass() {
		super();
		this.apiMethods = new HashSet<ApiMethod>();
	}

	public ApiClass(String name) {
		super();
		this.name = name;
		this.apiMethods = new HashSet<ApiMethod>();
	}

	public Set<ApiMethod> getApiMethods() {
		return apiMethods;
	}

	public Project getProject() {
		return project;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public void setApiMethods(Set<ApiMethod> apiMethods) {
		this.apiMethods = apiMethods;
	}

	public void setInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public void setProject(Project api) {
		this.project = api;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ApiClass [name=").append(name).append("]");
		return builder.toString();
	}

	public boolean isEnum() {
		return isEnum;
	}

	protected void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ApiClass))
			return false;
		ApiClass other = (ApiClass) obj;
		if (isEnum != other.isEnum)
			return false;
		if (isInterface != other.isInterface)
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

}
