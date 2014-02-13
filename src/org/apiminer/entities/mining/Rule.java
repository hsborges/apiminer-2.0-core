package org.apiminer.entities.mining;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apiminer.entities.api.ApiMethod;

@SuppressWarnings("serial")
@Entity
@Table(name = "Rule")
public class Rule implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "mining_result_id")
	private MiningResult miningResult;
	
	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "premise_id")
	private Itemset premise;

	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "consequence_id")
	private Itemset consequence;

	@Column(name = "total_support", columnDefinition = "integer", nullable = false)
	private Integer totalSupport;

	@Column(name = "total_transactions", columnDefinition = "integer", nullable = false)
	private Integer totalTransactions;
	
	public Rule() {
		super();
	}

	public Rule(MiningResult miningResult, Itemset premisse, Itemset consequence) {
		super();
		this.miningResult = miningResult;
		this.premise = premisse;
		this.consequence = consequence;
	}

	@Transient
	public Double getConfidence() {
		return RuleMetrics.CONFIDENCE.computeRelative(premise.getSupport(), consequence.getSupport(), totalSupport, totalTransactions);
	}

	public Itemset getConsequence() {
		return consequence;
	}

	@Transient
	public Double getConviction() {
		return RuleMetrics.CONVICTION.computeRelative(premise.getSupport(), consequence.getSupport(), totalSupport, totalTransactions);
	}

	public Long getId() {
		return id;
	}
	
	@Transient
	public Double getJaccard(){
		return RuleMetrics.JACCARD.computeRelative(premise.getSupport(), consequence.getSupport(), totalSupport, totalTransactions);
	}

	@Transient
	public Double getLeverage() {
		return RuleMetrics.LEVERAGE.computeRelative(premise.getSupport(), consequence.getSupport(), totalSupport, totalTransactions);
	}

	@Transient
	public Double getLift() {
		return RuleMetrics.LIFT.computeRelative(premise.getSupport(), consequence.getSupport(), totalSupport, totalTransactions);
	}

	public MiningResult getMiningResult() {
		return miningResult;
	}

	public Itemset getPremise() {
		return premise;
	}

	public Integer getTotalSupport() {
		return totalSupport;
	}

	public Integer getTotalTransactions() {
		return totalTransactions;
	}

	public void setConsequence(Itemset consequence) {
		this.consequence = consequence;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setMiningResult(MiningResult miningResult) {
		this.miningResult = miningResult;
	}
	
	public void setPremise(Itemset premisse) {
		this.premise = premisse;
	}

	public void setTotalSupport(Integer totalSupport) {
		this.totalSupport = totalSupport;
	}

	public void setTotalTransactions(Integer totalTransactions) {
		this.totalTransactions = totalTransactions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append("Rule [confidence=")
				.append(getConfidence())
				.append(", lift=")
				.append(getLift())
				.append(", leverage=")
				.append(getLeverage())
				.append(", conviction=")
				.append(getConviction())
				.append(", jaccard=")
				.append(getJaccard())
				.append("]");
		return builder.toString();
	}
	
	public SimpleRule getSimpleRule(){
		SimpleRule sr = new SimpleRule();
		sr.setOriginalRuleId(id);
		sr.setOriginalMiningResultId(miningResult.getId());
		
		sr.setPremiseSupport(premise.getSupport());
		for (ApiMethod premiseItem : premise.getElements()){
			sr.getPremise().add(premiseItem.getFullName());
		}
		
		sr.setConsequenceSupport(consequence.getSupport());
		for (ApiMethod consequenceItem : consequence.getElements()){
			sr.getConsequence().add(consequenceItem.getFullName());
		}
		
		sr.setTotalSupport(totalSupport);
		sr.setTotalTransactions(totalTransactions);
		
		sr.getMetrics().put(RuleMetrics.JACCARD.name(), getJaccard());
		sr.getMetrics().put(RuleMetrics.LEVERAGE.name(), getLeverage());
		sr.getMetrics().put(RuleMetrics.LIFT.name(), getLift());
		sr.getMetrics().put(RuleMetrics.CONFIDENCE.name(), getConfidence());
		sr.getMetrics().put(RuleMetrics.CONVICTION.name(), getConviction());
		
		return sr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((consequence == null) ? 0 : consequence.hashCode());
		result = prime * result + ((premise == null) ? 0 : premise.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Rule))
			return false;
		Rule other = (Rule) obj;
		if (consequence == null) {
			if (other.consequence != null)
				return false;
		} else if (!consequence.equals(other.consequence))
			return false;
		if (premise == null) {
			if (other.premise != null)
				return false;
		} else if (!premise.equals(other.premise))
			return false;
		return true;
	}

}
