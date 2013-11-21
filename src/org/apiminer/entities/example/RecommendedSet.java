package org.apiminer.entities.example;

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

import org.apiminer.daos.interfaces.IEntity;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.mining.SimpleRule;

@SuppressWarnings("serial")
@Entity
@Table(name = "RecommendedSet")
public class RecommendedSet implements IEntity, Comparable<RecommendedSet> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "RecommendedSet_MethodSet",
			joinColumns=@JoinColumn(name = "recommended_set_id"),
			inverseJoinColumns=@JoinColumn(name = "method_id")
	)
	private Set<ApiMethod> methodSet;
	
	@ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
	@JoinColumn(name = "rule_based_id")
	private SimpleRule ruleBased;
	
	@ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
	@JoinColumn(name = "recommended_combination_id")
	private RecommendedAssociation recommendedAssociation;
	
	@Column(name = "recommended_set_factor")
	private Double recommendedSetFactor;

	@ManyToMany(cascade = {CascadeType.REFRESH}) 
	@JoinTable(name = "RecommendedSet_Example", 
		joinColumns=@JoinColumn(name = "recommended_set_id"),
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

	public Set<ApiMethod> getMethodSet() {
		return methodSet;
	}

	public void setMethodSet(Set<ApiMethod> methodSet) {
		this.methodSet = methodSet;
	}

	public SimpleRule getRuleBased() {
		return ruleBased;
	}

	public void setRuleBased(SimpleRule ruleBased) {
		this.ruleBased = ruleBased;
	}

	public Double getRecommendedSetFactor() {
		return recommendedSetFactor;
	}

	public void setRecommendedSetFactor(Double recommendedSetFactor) {
		this.recommendedSetFactor = recommendedSetFactor;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecommendedSet [methodSet=").append(methodSet)
				.append(", ruleBased=").append(ruleBased)
				.append(", recommendedSetFactor=").append(recommendedSetFactor)
				.append("]");
		return builder.toString();
	}

	public RecommendedAssociation getRecommendedCombination() {
		return recommendedAssociation;
	}

	public void setRecommendedCombination(
			RecommendedAssociation recommendedAssociation) {
		this.recommendedAssociation = recommendedAssociation;
	}

	public List<Example> getRecommendedExamples() {
		return recommendedExamples;
	}

	public void setRecommendedExamples(List<Example> recommendedExamples) {
		this.recommendedExamples = recommendedExamples;
	}

	@Override
	public int compareTo(RecommendedSet o) {
		return recommendedSetFactor.compareTo(o.getRecommendedSetFactor());
	}
	
}
