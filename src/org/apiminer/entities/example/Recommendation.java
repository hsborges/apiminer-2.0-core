package org.apiminer.entities.example;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apiminer.entities.api.ApiElement;

@SuppressWarnings("serial")
@Entity
@Table(name = "Recommendation")
public class Recommendation implements Serializable {

	@Id
	@JoinColumn(name = "id", nullable = false)
	@ManyToOne(cascade={CascadeType.REFRESH})
	private ApiElement fromElement;
	
	@OneToMany(cascade = {CascadeType.ALL})
	@OrderColumn(name="associatedElementIndex")
	private List<AssociatedElement> associatedElements;
	
	@ManyToMany(cascade = {CascadeType.REFRESH}) 
	@JoinTable(name = "Recommendation_Example", 
		joinColumns=@JoinColumn(name = "recommendation_id"),
		inverseJoinColumns=@JoinColumn(name = "example_id")
	)
	@OrderColumn(name = "example_index")
	private List<Example> elementExamples;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at")
	private Date addedAt = new Date(System.currentTimeMillis());
	
	public ApiElement getFromElement() {
		return fromElement;
	}

	public void setFromElement(ApiElement fromElement) {
		this.fromElement = fromElement;
	}

	public List<AssociatedElement> getAssociatedElements() {
		return associatedElements;
	}

	public void setAssociatedElements(List<AssociatedElement> associatedElement) {
		this.associatedElements = associatedElement;
	}

	public List<Example> getElementExamples() {
		return elementExamples;
	}

	public void setElementExamples(List<Example> elementExamples) {
		this.elementExamples = elementExamples;
	}

	public Date getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	@Override
	public String toString() {
		return "Recommendation [fromElement=" + fromElement
				+ ", associatedElements=" + associatedElements
				+ ", elementExamples=" + elementExamples + ", addedAt="
				+ addedAt + "]";
	}

}
