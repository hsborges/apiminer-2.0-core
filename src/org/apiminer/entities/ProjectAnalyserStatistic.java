package org.apiminer.entities;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apiminer.entities.api.Project;

@SuppressWarnings("serial")
@Entity
@Table(name = "ProjectAnalyserStatistic")
public class ProjectAnalyserStatistic implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@OneToOne(mappedBy = "statistics", cascade = {CascadeType.REFRESH })
	private Project project;

	@Column(name = "method_declaration", columnDefinition = "bigint", nullable = false)
	private long numOfMethodDeclaration = 0;
	@Column(name = "accepted_method_declaration", columnDefinition = "bigint", nullable = false)
	private long numOfAcceptedMethodDeclaration = 0;

	@Column(name = "class_declaration", columnDefinition = "bigint", nullable = false)
	private long numOfClassDeclaration = 0;
	@Column(name = "accepted_class_declaration", columnDefinition = "bigint", nullable = false)
	private long numOfAcceptedClassDeclaration = 0;

	@Column(name = "constructor_invocation", columnDefinition = "bigint", nullable = false)
	private long numOfConstructorInvocation = 0;
	@Column(name = "accepted_constructor_invocation", columnDefinition = "bigint", nullable = false)
	private long numOfAcceptedConstructorInvocation = 0;

	@Column(name = "method_invocations", columnDefinition = "bigint", nullable = false)
	private long numOfMethodInvocations = 0;
	@Column(name = "accepted_method_invocations", columnDefinition = "bigint", nullable = false)
	private long numOfAcceptedMethodInvocations = 0;

	public ProjectAnalyserStatistic() {
		super();
	}

	public void decreaseNumOfAcceptedClassDeclaration() {
		numOfAcceptedClassDeclaration--;
	}

	public void decreaseNumOfAcceptedConstructorInvocation() {
		numOfAcceptedConstructorInvocation--;
	}

	public void decreaseNumOfAcceptedMethodDeclaration() {
		numOfAcceptedMethodDeclaration--;
	}

	public void decreaseNumOfAcceptedMethodInvocations() {
		numOfAcceptedMethodInvocations--;
	}

	public void decreaseNumOfClassDeclaration() {
		numOfClassDeclaration--;
	}

	public void decreaseNumOfConstructorInvocation() {
		numOfConstructorInvocation--;
	}

	public void decreaseNumOfMethodDeclaration() {
		numOfMethodDeclaration--;
	}

	public void decreaseNumOfMethodInvocations() {
		numOfMethodInvocations--;
	}

	public void increaseNumOfAcceptedClassDeclaration() {
		numOfAcceptedClassDeclaration++;
	}

	public void increaseNumOfAcceptedConstructorInvocation() {
		numOfAcceptedConstructorInvocation++;
	}

	public void increaseNumOfAcceptedMethodDeclaration() {
		numOfAcceptedMethodDeclaration++;
	}

	public void increaseNumOfAcceptedMethodInvocations() {
		numOfAcceptedMethodInvocations++;
	}

	public void increaseNumOfClassDeclaration() {
		numOfClassDeclaration++;
	}

	public void increaseNumOfConstructorInvocation() {
		numOfConstructorInvocation++;
	}

	public void increaseNumOfMethodDeclaration() {
		numOfMethodDeclaration++;
	}

	public void increaseNumOfMethodInvocations() {
		numOfMethodInvocations++;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public long getNumOfMethodDeclaration() {
		return numOfMethodDeclaration;
	}

	public void setNumOfMethodDeclaration(long numOfMethodDeclaration) {
		this.numOfMethodDeclaration = numOfMethodDeclaration;
	}

	public long getNumOfAcceptedMethodDeclaration() {
		return numOfAcceptedMethodDeclaration;
	}

	public void setNumOfAcceptedMethodDeclaration(
			long numOfAcceptedMethodDeclaration) {
		this.numOfAcceptedMethodDeclaration = numOfAcceptedMethodDeclaration;
	}

	public long getNumOfClassDeclaration() {
		return numOfClassDeclaration;
	}

	public void setNumOfClassDeclaration(long numOfClassDeclaration) {
		this.numOfClassDeclaration = numOfClassDeclaration;
	}

	public long getNumOfAcceptedClassDeclaration() {
		return numOfAcceptedClassDeclaration;
	}

	public void setNumOfAcceptedClassDeclaration(long numOfAcceptedClassDeclaration) {
		this.numOfAcceptedClassDeclaration = numOfAcceptedClassDeclaration;
	}

	public long getNumOfConstructorInvocation() {
		return numOfConstructorInvocation;
	}

	public void setNumOfConstructorInvocation(long numOfConstructorInvocation) {
		this.numOfConstructorInvocation = numOfConstructorInvocation;
	}

	public long getNumOfAcceptedConstructorInvocation() {
		return numOfAcceptedConstructorInvocation;
	}

	public void setNumOfAcceptedConstructorInvocation(
			long numOfAcceptedConstructorInvocation) {
		this.numOfAcceptedConstructorInvocation = numOfAcceptedConstructorInvocation;
	}

	public long getNumOfMethodInvocations() {
		return numOfMethodInvocations;
	}

	public void setNumOfMethodInvocations(long numOfMethodInvocations) {
		this.numOfMethodInvocations = numOfMethodInvocations;
	}

	public long getNumOfAcceptedMethodInvocations() {
		return numOfAcceptedMethodInvocations;
	}

	public void setNumOfAcceptedMethodInvocations(
			long numOfAcceptedMethodInvocations) {
		this.numOfAcceptedMethodInvocations = numOfAcceptedMethodInvocations;
	}
	
}
