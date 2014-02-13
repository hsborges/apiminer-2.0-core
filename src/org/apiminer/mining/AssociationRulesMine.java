package org.apiminer.mining;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.MiningDAO;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.mining.Itemset;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.entities.mining.Rule;
import org.apiminer.util.WekaUtil;

import weka.associations.AbstractAssociator;
import weka.associations.FPGrowth;
import weka.associations.FPGrowth.AssociationRule;
import weka.associations.FPGrowth.BinaryItem;
import weka.core.Instances;

/**
 * @author Hudson Silva Borges
 * 
 */
public class AssociationRulesMine {

	private static final Logger LOGGER = Logger.getLogger(AssociationRulesMine.class);

	private double minConvidenceValue = Double.valueOf(0.9);
	private int minSupportValue = 0;
	private int maxElements = 4;

	public MiningResult build(Class<? extends AbstractAssociator> algorithm) throws Exception {
		if (algorithm == FPGrowth.class) {
			return FPGrowth(false);
		} else {
			throw new IllegalArgumentException("Algorithm not implemented!");
		}
	}
	
	public void buildAndPersist(Class<? extends AbstractAssociator> miningClass) throws Exception {
		if (miningClass == FPGrowth.class) {
			FPGrowth(true);
		} else {
			throw new IllegalArgumentException("Algorithm not implemented!");
		}
	}
	
	private MiningResult FPGrowth(boolean incrementalPersistence) throws Exception {
		LOGGER.debug("Mining association rules with algorithm " + FPGrowth.class.getName());
		
		Instances instances = WekaUtil.getSparseInstanceWeka(minSupportValue, false);

		LOGGER.debug("Number of instances: " + instances.numInstances());
		LOGGER.debug("Number of attributes: " + instances.numAttributes());

		// Compute the relative support from absolute support value
		double relativeMinSupportValue = ((minSupportValue * 100) / Double.valueOf(instances.numInstances())) / Double.valueOf(100);
		
		// Instantiate and set parameters
		FPGrowth growth = new FPGrowth();
		growth.setLowerBoundMinSupport(relativeMinSupportValue);
		growth.setMinMetric(minConvidenceValue);
		
		// This parameter is needed for sparse instances
		growth.setPositiveIndex(2);
		
		//FIXME Evaluate if the number of max items is good for the applications
		growth.setMaxNumberOfItems(maxElements);
		growth.setNumRulesToFind(Integer.MAX_VALUE);
		
		//FIXME Evaluate if all rules are needed for the specified support
		growth.setFindAllRulesForSupportLevel(true);

		LOGGER.debug(String.format("Building Associations with params: %s",Arrays.toString(growth.getOptions())));

		try {
			if (instances.numAttributes() > 0 && instances.numInstances() > 0) {
				//Build associations
				growth.buildAssociations(instances);
			}
		} catch (Throwable e) {
			throw new Exception(e);
		}

		System.gc();
		
		LOGGER.debug("Build finished! Building model.");

		ProjectDAO projectDAO = new ProjectDAO();
		MiningDAO miningDAO = new MiningDAO();
		
		List<AssociationRule> growthRules = growth.getAssociationRules();
		
		LOGGER.debug(String.format("Number of rules discovered: %d",growthRules == null ? 0 : growthRules.size()));
		
		Project api = projectDAO.findSourceAPI();
		
		List<Project> clients = projectDAO.findAllClients(DatabaseType.PRE_PROCESSING);

		// Handling the findings
		MiningResult result = new MiningResult(api, clients);
		result.setAlgorithm(FPGrowth.class.getName());
		result.setFromAPI(api);
		result.setFromProjects(clients);
		result.setOptions(Arrays.toString(growth.getOptions()));
		result.setNumberOfApiElements(instances.numAttributes());
		result.setNumberOfTransactions(instances.numInstances());
		
		if (incrementalPersistence) {
			miningDAO.beginTransaction();
			miningDAO.incrementalPersist(result);
			LOGGER.debug("Persisting temporally the results.");
		}
		
		ApiMethodDAO methodDAO = new ApiMethodDAO();
		if (growthRules != null) {
			
			int rulesCounter = 1;
			int totalRules = growth.getAssociationRules().size();
			int interval = totalRules / 10;
			int intervalCounter = 1;
			char[] bar = new char[10];
			Arrays.fill(bar, ' ');
		
			for (AssociationRule ar : growth.getAssociationRules()) {
				Set<ApiMethod> premisseElementList = new HashSet<ApiMethod>();
				for (BinaryItem item : ar.getPremise()) {
					if (incrementalPersistence) {
						ApiMethod method = new ApiMethod();
						method.setId(Long.parseLong(item.getAttribute().name()));
						premisseElementList.add(method);
					}else{
						premisseElementList.add((ApiMethod) methodDAO.find(Long.parseLong(item.getAttribute().name()), DatabaseType.PRE_PROCESSING));
					}
				}
				Itemset premisseItemset = new Itemset(premisseElementList, ar.getPremiseSupport());

				Set<ApiMethod> consequenceElementList = new HashSet<ApiMethod>();
				for (BinaryItem item : ar.getConsequence()) {
					if (incrementalPersistence) { 
						ApiMethod method = new ApiMethod();
						method.setId(Long.parseLong(item.getAttribute().name()));
						consequenceElementList.add(method);
					}else{
						consequenceElementList.add((ApiMethod) methodDAO.find(Long.parseLong(item.getAttribute().name()), DatabaseType.PRE_PROCESSING));
					}
				}
				Itemset consequenceItemset = new Itemset(consequenceElementList, ar.getConsequenceSupport());

				Rule rule = new Rule(result, premisseItemset, consequenceItemset);
				rule.setTotalSupport(ar.getTotalSupport());
				rule.setTotalTransactions(ar.getTotalTransactions());

				if (incrementalPersistence) {
					if ((interval * intervalCounter) == rulesCounter++){
						try {
							bar[intervalCounter++ - 1] = '=';
							LOGGER.debug(String.format("Progress [%s] (%d remaining)", new String(bar), totalRules - rulesCounter));
						} catch (Exception e) {}
					}
					miningDAO.incrementalPersist(rule);
				}else{
					result.getRules().add(rule);
				}

			}
		}
		
		if (incrementalPersistence) {
			miningDAO.commitAndCloseTransaction();
			LOGGER.debug("Making permanent the persisted data.");
		}
		
		LOGGER.debug("Asociation rules mining finished!");
		
		return result;
	}

	public double getMinMetricValue() {
		return minConvidenceValue;
	}

	public void setMinConfidenceValue(double minMetricValue) {
		this.minConvidenceValue = minMetricValue;
	}

	public int getLowerBoundMinSupportValue() {
		return minSupportValue;
	}

	public void setMinSupportValue(int lowerBoundMinSupportValue) {
		this.minSupportValue = lowerBoundMinSupportValue;
	}

	public int getMaxNumberOfItens() {
		return maxElements;
	}

	public void setMaxNumberOfElements(int maxNumberOfItens) {
		this.maxElements = maxNumberOfItens;
	}

}
