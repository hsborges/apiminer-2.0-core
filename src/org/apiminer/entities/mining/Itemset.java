package org.apiminer.entities.mining;

import java.io.Serializable;
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
import javax.persistence.Table;

import org.apiminer.entities.api.ApiMethod;

@SuppressWarnings("serial")
@Entity
@Table(name = "Itemset")
public class Itemset implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "support", columnDefinition = "integer", nullable = false)
	private Integer support;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "Itemset_ApiMethod", 
		joinColumns=@JoinColumn(name = "itemset_id"),
		inverseJoinColumns=@JoinColumn(name = "api_method_id")
	)
	private Set<ApiMethod> elements;
	
	public Itemset() {
		super();
	}

	public Itemset(Set<ApiMethod> elements, Integer support) {
		this.elements = elements;
		this.support = support;
	}

	public Set<ApiMethod> getElements() {
		return elements;
	}

	public void setElements(Set<ApiMethod> elements) {
		this.elements = elements;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSupport() {
		return support;
	}

	public void setSupport(Integer support) {
		this.support = support;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((elements == null) ? 0 : elements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Itemset))
			return false;
		Itemset other = (Itemset) obj;
		if (elements == null) {
			if (other.elements != null)
				return false;
		} else if (!elements.equals(other.elements))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Itemset [id=").append(id).append(", support=")
				.append(support).append(", elements=").append(elements)
				.append("]");
		return builder.toString();
	}

}
