package org.apiminer.entities.mining;

import java.io.Serializable;
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
import javax.persistence.Table;

import org.apiminer.entities.api.ApiMethod;

@SuppressWarnings("serial")
@Entity
@Table(name = "Transaction")
public class Transaction implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "source_api_method_id")
	private ApiMethod sourceApiMethod;
	
	@ManyToMany(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinTable(name = "Transaction_Invocation", 
		joinColumns=@JoinColumn(name = "transaction_id"),
		inverseJoinColumns=@JoinColumn(name = "api_method_id")
	)
	private Set<ApiMethod> invocations;
	
	public Transaction() {
		super();
	}

	public Transaction(ApiMethod apiMethod, Set<ApiMethod> invocations) {
		super();
		this.sourceApiMethod = apiMethod;
		this.invocations = invocations;
	}

	public ApiMethod getSourceApiMethod() {
		return sourceApiMethod;
	}

	public Long getId() {
		return id;
	}

	public Set<ApiMethod> getInvocations() {
		return invocations;
	}

	public void setSourceApiMethod(ApiMethod apiMethod) {
		this.sourceApiMethod = apiMethod;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setInvocations(Set<ApiMethod> invocations) {
		this.invocations = invocations;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Transaction))
			return false;
		Transaction other = (Transaction) obj;
		if (sourceApiMethod == null) {
			if (other.sourceApiMethod != null)
				return false;
		} else if (!sourceApiMethod.equals(other.sourceApiMethod))
			return false;
		if (invocations == null) {
			if (other.invocations != null)
				return false;
		} else if (!invocations.equals(other.invocations))
			return false;
		return true;
	}
	
}
