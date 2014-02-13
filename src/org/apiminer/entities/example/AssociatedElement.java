package org.apiminer.entities.example;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.mining.SimpleRule;

@SuppressWarnings("serial")
@Entity
@Table(name = "AssociatedElement")
public class AssociatedElement implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "AssociatedElement_Element",
			joinColumns=@JoinColumn(name = "associated_element_id"),
			inverseJoinColumns=@JoinColumn(name = "element_id")
	)
	private Set<ApiElement> elements;
	
	@ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "rule_based_id")
	private SimpleRule ruleBased;
	
	@ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
	@JoinColumn(name = "recommendation_id")
	private Recommendation recommendation;
	
	@ManyToMany(cascade = {CascadeType.REFRESH}) 
	@JoinTable(name = "AssociatedElement_Example", 
		joinColumns=@JoinColumn(name = "associated_element_id"),
		inverseJoinColumns=@JoinColumn(name = "example_id")
	)
	@OrderColumn(name = "example_index")
	private List<Example> recommendedExamples;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Set<ApiElement> getElements() {
		return elements;
	}

	public void setElements(Set<ApiElement> elements) {
		this.elements = elements;
	}

	public SimpleRule getRuleBased() {
		return ruleBased;
	}

	public void setRuleBased(SimpleRule ruleBased) {
		this.ruleBased = ruleBased;
	}

	public Recommendation getRecommendedAssociations() {
		return recommendation;
	}

	public void setRecommendedAssociations(Recommendation recommendation) {
		this.recommendation = recommendation;
	}

	public List<Example> getRecommendedExamples() {
		return recommendedExamples;
	}

	public void setRecommendedExamples(List<Example> recommendedExamples) {
		this.recommendedExamples = recommendedExamples;
	}
	
}
