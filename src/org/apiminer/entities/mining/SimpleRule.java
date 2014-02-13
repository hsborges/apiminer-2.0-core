package org.apiminer.entities.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name= "SimpleRule")
public class SimpleRule implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "original_mining_result_id", nullable = false)
	private Long originalMiningResultId;
	
	@Column(name = "original_rule_id", nullable = false)
	private Long originalRuleId;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "SimpleRule_Premise", joinColumns=@JoinColumn(name = "simple_rule_id"))
	@OrderColumn(name= "premise_index")
	private List<String> premise;
	
	@Column(name ="premise_support", columnDefinition = "integer", nullable = false)
	private Integer premiseSupport;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "SimpleRule_Consequence", joinColumns=@JoinColumn(name = "simple_rule_id"))
	@OrderColumn(name= "consequence_index")
	private List<String> consequence;
	
	@Column(name ="consequence_support", columnDefinition = "integer", nullable = false)
	private Integer consequenceSupport;

	@Column(name = "total_support", columnDefinition = "integer", nullable = false)
	private Integer totalSupport;

	@Column(name = "total_transactions", columnDefinition = "integer", nullable = false)
	private Integer totalTransactions;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "SimpleRule_Metrics", joinColumns=@JoinColumn(name = "simple_rule_id"))
	private Map<String, Double> metrics;
	
	public SimpleRule() {
		super();
		this.premise = new ArrayList<String>();
		this.consequence = new ArrayList<String>();
		this.metrics = new HashMap<String, Double>();
	}

	public Long getOriginalMiningResultId() {
		return originalMiningResultId;
	}

	public void setOriginalMiningResultId(Long originalMiningResultId) {
		this.originalMiningResultId = originalMiningResultId;
	}

	public Long getOriginalRuleId() {
		return originalRuleId;
	}

	public void setOriginalRuleId(Long originalRuleId) {
		this.originalRuleId = originalRuleId;
	}

	public List<String> getPremise() {
		return premise;
	}

	public void setPremise(List<String> premise) {
		this.premise = premise;
	}

	public Integer getPremiseSupport() {
		return premiseSupport;
	}

	public void setPremiseSupport(Integer premiseSupport) {
		this.premiseSupport = premiseSupport;
	}

	public List<String> getConsequence() {
		return consequence;
	}

	public void setConsequence(List<String> consequence) {
		this.consequence = consequence;
	}

	public Integer getConsequenceSupport() {
		return consequenceSupport;
	}

	public void setConsequenceSupport(Integer consequenceSupport) {
		this.consequenceSupport = consequenceSupport;
	}

	public Integer getTotalSupport() {
		return totalSupport;
	}

	public void setTotalSupport(Integer totalSupport) {
		this.totalSupport = totalSupport;
	}

	public Integer getTotalTransactions() {
		return totalTransactions;
	}

	public void setTotalTransactions(Integer totalTransactions) {
		this.totalTransactions = totalTransactions;
	}

	public Map<String, Double> getMetrics() {
		return metrics;
	}

	public void setMetrics(Map<String, Double> metrics) {
		this.metrics = metrics;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SimpleRule [premiseSupport=").append(premiseSupport)
				.append(", consequenceSupport=").append(consequenceSupport)
				.append(", totalSupport=").append(totalSupport)
				.append(", totalTransactions=").append(totalTransactions)
				.append(", metrics=").append(metrics).append("]");
		return builder.toString();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	
}
