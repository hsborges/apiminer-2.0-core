package org.apiminer.entities.mining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apiminer.entities.api.Project;

@SuppressWarnings("serial")
@Entity
@Table(name = "MiningResult")
public class MiningResult implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(optional=false, cascade = {CascadeType.REFRESH})
	@JoinColumn(name = "from_api_id")
	private Project fromAPI;

	@Column(name = "algorithm", columnDefinition = "varchar(254)", nullable=false)
	private String algorithm;
	
	@ManyToMany(cascade = {CascadeType.REFRESH})
	@JoinTable(name = "MiningResult_Project", 
		joinColumns=@JoinColumn(name = "mining_result_id"),
		inverseJoinColumns=@JoinColumn(name = "project_id")
	)
	@OrderColumn(name = "project_index")
	@OrderBy("name")
	private List<Project> fromProjects;	

	@OneToMany(mappedBy = "miningResult", cascade = {CascadeType.ALL})
	@OrderBy(value = "totalSupport DESC")
	private List<Rule> rules;

	@Column(name = "options", columnDefinition = "text", nullable=false)
	private String options;

	@Column(name = "number_of_transactions", columnDefinition = "integer", nullable=false)
	private Integer numberOfTransactions;

	@Column(name = "number_of_api_elements", columnDefinition = "integer", nullable=false)
	private Integer numberOfApiElements;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "added_at", columnDefinition = "timestamp", nullable=false)
	private Date addedAt;
	
	public MiningResult() {
		super();
		this.rules = new ArrayList<Rule>();
		this.addedAt = new Date(System.currentTimeMillis());
	}

	public MiningResult(Project fromAPI, List<Project> fromProjects) {
		super();
		this.fromAPI = fromAPI;
		this.fromProjects = fromProjects;
		this.rules = new ArrayList<Rule>();
		this.addedAt = new Date(System.currentTimeMillis());
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the fromAPI
	 */
	public Project getFromAPI() {
		return fromAPI;
	}

	/**
	 * @param fromAPI the fromAPI to set
	 */
	public void setFromAPI(Project fromAPI) {
		this.fromAPI = fromAPI;
	}

	/**
	 * @return the addedAt
	 */
	public Date getAddedAt() {
		return addedAt;
	}

	/**
	 * @param addedAt the addedAt to set
	 */
	public void setAddedAt(Date addedAt) {
		this.addedAt = addedAt;
	}

	/**
	 * @return the algorithm
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * @param algorithm the algorithm to set
	 */
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	/**
	 * @return the rules
	 */
	public List<Rule> getRules() {
		return rules;
	}

	/**
	 * @param rules the rules to set
	 */
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the numberOfTransactions
	 */
	public Integer getNumberOfTransactions() {
		return numberOfTransactions;
	}

	/**
	 * @param numberOfTransactions the numberOfTransactions to set
	 */
	public void setNumberOfTransactions(Integer numberOfTransactions) {
		this.numberOfTransactions = numberOfTransactions;
	}

	/**
	 * @return the numberOfApiElements
	 */
	public Integer getNumberOfApiElements() {
		return numberOfApiElements;
	}

	/**
	 * @param numberOfApiElements the numberOfApiElements to set
	 */
	public void setNumberOfApiElements(Integer numberOfApiElements) {
		this.numberOfApiElements = numberOfApiElements;
	}

	/**
	 * @return the fromProjects
	 */
	public List<Project> getFromProjects() {
		return fromProjects;
	}

	/**
	 * @param fromProjects the fromProjects to set
	 */
	public void setFromProjects(List<Project> projects) {
		this.fromProjects = projects;
	}

	@Override
	public String toString() {
		return "MiningResult [id=" + id + ", fromAPI=" + fromAPI
				+ ", algorithm=" + algorithm + ", numberOfTransactions="
				+ numberOfTransactions + ", numberOfApiElements="
				+ numberOfApiElements + ", addedAt=" + addedAt + "]";
	}

}
