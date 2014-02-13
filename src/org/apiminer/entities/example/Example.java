package org.apiminer.entities.example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apiminer.entities.Attachment;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.Project;

/**
 * The persistent class for the example database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "Example")
public class Example implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	@Basic(fetch=FetchType.LAZY)
	@Column(name = "code_example", columnDefinition = "text", nullable = false)
	private String codeExample;
	
	@Column(name = "formatted_code_example", columnDefinition = "text", nullable = false)
	private String formattedCodeExample;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "Example_Seed")
	@OrderColumn(name = "seed_index")
	private List<String> seeds;

	@ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "Example_ApiElement", 
		joinColumns=@JoinColumn(name = "example_id"),
		inverseJoinColumns=@JoinColumn(name = "element_id")
	)
	private Set<ApiElement> apiElements;

	@Basic(fetch=FetchType.LAZY)
	@Column(name = "source_method", columnDefinition = "bytea")
	private byte[] sourceMethod;
	
	@Column(name = "source_method_call", columnDefinition = "text", nullable = false)
	private String sourceMethodCall;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at", columnDefinition = "timestamp")
	private Date addedAt;
	
	@OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "attachment_id")
	private Attachment attachment;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "Example_Metrics", joinColumns=@JoinColumn(name = "example_id"))
	private Map<String, Integer> metrics;
	
	@OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy="example")
	@OrderBy("addedAt")
	private List<ExampleFeedback> feedbacks;
	
	@ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY, mappedBy="recommendedExamples") 
	@JoinTable(name = "RecommendedSet_Example", 
		joinColumns=@JoinColumn(name = "example_id"),
		inverseJoinColumns=@JoinColumn(name = "recommended_set_id")
	)
	@OrderBy("id")
	private List<AssociatedElement> associatedElement;
	
	@Column(name = "has_problems")
	private boolean hasProblems = false;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "Example_Imports")
	private Set<String> imports;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "Example_Problems")
	@OrderColumn(name = "index")
	private List<Integer> problems;

	public Example() {
		super();
		this.metrics = new HashMap<String, Integer>();
		this.seeds = new LinkedList<String>();
		this.imports = new HashSet<String>();
		this.problems = new LinkedList<Integer>();
	}

	public Date getAddedAt() {
		return addedAt;
	}


	public String getCodeExample() {
		return codeExample;
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public List<String> getSeeds() {
		return seeds;
	}

	@Transient
	public int numberOfLines(){
		return Math.max(codeExample.split("\n").length, codeExample.split("\r").length);
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public void setCodeExample(String codeExample) {
		this.codeExample = codeExample;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setSeeds(List<String> seeds) {
		this.seeds = seeds;
	}

	public String getSourceMethod() {
		return new String(sourceMethod);
	}

	public void setSourceMethod(String sourceFile) {
		this.sourceMethod = sourceFile.getBytes();
	}

	public String getFormattedCodeExample() {
		return formattedCodeExample;
	}

	public void setFormattedCodeExample(String formattedCodeExample) {
		this.formattedCodeExample = formattedCodeExample;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Example [id=").append(id).append(", project=")
				.append(project).append(", codeExample=").append(codeExample)
				.append(", formattedCodeExample=").append(formattedCodeExample)
				.append(", seeds=").append(seeds).append(", apiMethods=")
				.append(apiElements).append(", sourceMethod=")
				.append(Arrays.toString(sourceMethod)).append(", addedAt=")
				.append(addedAt).append("]");
		return builder.toString();
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public String getSourceMethodCall() {
		return sourceMethodCall;
	}

	public void setSourceMethodCall(String sourceMethodCall) {
		this.sourceMethodCall = sourceMethodCall;
	}

	public List<ExampleFeedback> getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(List<ExampleFeedback> feedbacks) {
		this.feedbacks = feedbacks;
	}

	public List<AssociatedElement> getRecommendedSets() {
		return associatedElement;
	}

	public void setRecommendedSets(List<AssociatedElement> associatedElement) {
		this.associatedElement = associatedElement;
	}

	public Map<String, Integer> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Integer> metrics) {
		this.metrics = metrics;
	}

	public boolean isHasProblems() {
		return hasProblems;
	}

	public void setHasProblems(boolean hasProblems) {
		this.hasProblems = hasProblems;
	}

	public void setSourceMethod(byte[] sourceMethod) {
		this.sourceMethod = sourceMethod;
	}

	public Set<String> getImports() {
		return imports;
	}

	public void setImports(Set<String> imports) {
		this.imports = imports;
	}

	public List<Integer> getProblems() {
		return problems;
	}

	public void setProblems(List<Integer> problems) {
		this.problems = problems;
	}

	public Set<ApiElement> getApiMethods() {
		return apiElements;
	}

	public void setApiMethods(Set<ApiElement> apiMethods) {
		this.apiElements = apiMethods;
	}

	public List<AssociatedElement> getRecommendedElements() {
		return associatedElement;
	}

	public void setRecommendedElements(List<AssociatedElement> associatedElement) {
		this.associatedElement = associatedElement;
	}


}