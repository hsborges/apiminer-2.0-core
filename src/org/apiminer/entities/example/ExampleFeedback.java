package org.apiminer.entities.example;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the example_feedback database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "ApiExampleFeedback")
public class ExampleFeedback implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "comment", columnDefinition = "text", nullable = true)
	private String comment;

	@Column(name = "rating", columnDefinition = "smallint", nullable = false)
	private int rating;

	@ManyToOne(optional = false, cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "example_id")
	private Example example;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at", columnDefinition = "timestamp", nullable = false)
	private Date addedAt;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getComment() {
		return this.comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getRating() {
		return this.rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Example getExample() {
		return this.example;
	}

	public void setExample(Example example) {
		this.example = example;
	}

	public Date getDate() {
		return addedAt;
	}

	public void setDate(Date addedAt) {
		this.addedAt = addedAt;
	}

}