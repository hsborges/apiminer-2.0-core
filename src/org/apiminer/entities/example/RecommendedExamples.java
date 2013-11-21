package org.apiminer.entities.example;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.apiminer.daos.interfaces.IEntity;
import org.apiminer.entities.api.ApiMethod;

@SuppressWarnings("serial")
@Entity
@Table(name = "RecommendedExamples")
public class RecommendedExamples implements IEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "Recommendation_ApiMethod", 
			joinColumns=@JoinColumn(name = "recommendation_id"),
			inverseJoinColumns=@JoinColumn(name = "api_method_id")
	)
	private Set<ApiMethod> fromApiMethods;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "Recommendation_Example", 
		joinColumns=@JoinColumn(name = "recommendation_id"),
		inverseJoinColumns=@JoinColumn(name = "example_id")
	)
	@OrderColumn(name = "recommendation_index")
	private List<Example> recommendedExamples;
	
	@Column(name = "mining_result_id", columnDefinition = "bigint")
	private long miningResultId;
	
	public Set<ApiMethod> getFromApiMethods() {
		return fromApiMethods;
	}

	public Long getId() {
		return id;
	}

	public List<Example> getRecommendedExamples() {
		return recommendedExamples;
	}

	public void setFromApiMethods(Set<ApiMethod> apiMethod) {
		this.fromApiMethods = apiMethod;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setRecommendedExamples(List<Example> recommendedExamples) {
		this.recommendedExamples = recommendedExamples;
	}

	public long getMiningResultId() {
		return miningResultId;
	}

	public void setMiningResultId(long miningResultId) {
		this.miningResultId = miningResultId;
	}
	
}
