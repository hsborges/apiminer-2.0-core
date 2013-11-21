package org.apiminer.entities.example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apiminer.entities.api.ApiMethod;

@SuppressWarnings("serial")
@Entity
@Table(name = "RecommendedCombination")
public class RecommendedAssociation implements Serializable {

	@Id
	@Column(name = "id")
	private String id;

	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "RecommendedCombination_ApiMethod",
			joinColumns=@JoinColumn(name = "recommended_combination_id"),
			inverseJoinColumns=@JoinColumn(name = "api_method_id")
	)
	private Set<ApiMethod> fromMethods;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@OrderBy(value="recommendedSetFactor DESC")
	private List<RecommendedSet> recommendedSets;
	
	@Column(name = "based_on_premise")
	private boolean basedOnPremise;
	
	@Column(name = "based_on_consequence")
	private boolean basedOnConsequence;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at")
	private Date addedAt = new Date(System.currentTimeMillis());

	public Set<ApiMethod> getFromMethods() {
		return fromMethods;
	}

	public void setFromMethods(Set<ApiMethod> fromMethods) {
		this.fromMethods = fromMethods;
	}

	public List<RecommendedSet> getRecommendedSets() {
		return recommendedSets;
	}

	public void setRecommendedSets(List<RecommendedSet> recommendedSets) {
		this.recommendedSets = recommendedSets;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public static final String generateKey(RecommendedAssociation recommendedAssociation){
		int size = recommendedAssociation.getFromMethods().size();
		long[] ids = new long[size];
		for (ApiMethod apm : recommendedAssociation.getFromMethods()){
			ids[--size] = apm.getId();
		}
		Arrays.sort(ids);
		
		return Arrays.toString(ids).trim().replace(" ","");
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RecommendedCombination [id=").append(id)
				.append(", fromMethods=").append(fromMethods)
				.append(", addedAt=").append(addedAt).append("]");
		return builder.toString();
	}

	public boolean isBasedOnPremise() {
		return basedOnPremise;
	}

	public void setBasedOnPremise(boolean basedOnPremise) {
		this.basedOnPremise = basedOnPremise;
	}

	public boolean isBasedOnConsequence() {
		return basedOnConsequence;
	}

	public void setBasedOnConsequence(boolean basedOnConsequence) {
		this.basedOnConsequence = basedOnConsequence;
	}
	
	@PrePersist
	@PreUpdate
	public void prePersist(){
		this.id = generateKey(this);
	}
	
}
