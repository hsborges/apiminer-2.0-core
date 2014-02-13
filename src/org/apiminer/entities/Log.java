package org.apiminer.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("serial")
@Entity
@Table(name = "Log")
public class Log implements Serializable {
	
	public static final String GET_EXAMPLE = "GET_EXAMPLE";
	public static final String GET_RECOMMENDATION = "GET_RECOMMENDATION";
	public static final String GET_FULL_CODE = "GET_FULL_CODE";
	public static final String FEEDBACK_EXAMPLE = "FEEDBACK_EXAMPLE";
	
	public static final String ANONYMOUS_USER = "ANONYMOUS_USER";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String type;
	
	@Column(name = "example_id")
	private Long exampleId;
	
	@Column(name = "example_index")
	private Integer exampleIndex;
	
	@Column(name = "api_method_id")
	private Long apiMethodId;
	
	@Column(name = "api_method_full_name")
	private String apiMethodFullName;
	
	private Boolean feedback;
	
	@Column(name = "user_email")
	private String userEmail = ANONYMOUS_USER;
	
	@Column(name = "recommended_set")
	private Long recommendedSetId;
	
	@Column(name = "added_at")
	@Temporal(TemporalType.TIMESTAMP)
	private Date addedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getExampleId() {
		return exampleId;
	}

	public void setExampleId(Long exampleId) {
		this.exampleId = exampleId;
	}

	public Integer getExampleIndex() {
		return exampleIndex;
	}

	public void setExampleIndex(Integer exampleIndex) {
		this.exampleIndex = exampleIndex;
	}

	public Long getApiMethodId() {
		return apiMethodId;
	}

	public void setApiMethodId(Long apiMethodId) {
		this.apiMethodId = apiMethodId;
	}

	public String getApiMethodFullName() {
		return apiMethodFullName;
	}

	public void setApiMethodFullName(String apiMethodFullName) {
		this.apiMethodFullName = apiMethodFullName;
	}

	public Boolean getFeedback() {
		return feedback;
	}

	public void setFeedback(Boolean feedback) {
		this.feedback = feedback;
	}

	public String getUser() {
		return userEmail;
	}

	public void setUser(String user) {
		this.userEmail = user;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	public Long getRecommendedSetId() {
		return recommendedSetId;
	}

	public void setRecommendedSetId(Long recommendedSetId) {
		this.recommendedSetId = recommendedSetId;
	}
	
}
