package org.apiminer.recommendation.associations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.daos.ItemsetDAO;
import org.apiminer.daos.RuleDAO;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.RecommendedAssociation;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.entities.mining.Itemset;
import org.apiminer.entities.mining.Rule;
import org.apiminer.recommendation.AbstractRecommenderCriteria;

/**
 * 	Classe responsável por buscar e recomendar combinações de chamadas de métodos baseando-se 
 *  nas regras de associação mineradas.
 * 
 * @author Hudson S. Borges
 *
 */
public class CombinationRecommender {
	
	private final Logger logger = Logger.getLogger(CombinationRecommender.class);
	
	private final RuleDAO ruleDAO = new RuleDAO();
	
	private final Set<AbstractRecommenderCriteria<RecommendedSet>> criterias = new HashSet<AbstractRecommenderCriteria<RecommendedSet>>();
	
	private long miningResultId;
	
	/**
	 * @param miningResultId Identificador do resultado de mineração a ser utilizado 
	 */
	public CombinationRecommender(long miningResultId){
		this.miningResultId = miningResultId;
	}
	
	/**
	 * 	Busca todas as recomendações de combinações para as regras contidas no resultado da mineração indicado
	 * 
	 * @return Lista com todas combinações recomendadas
	 */
	public List<RecommendedAssociation> findAllRecommendedCombination(){
		return findAllRecommendedCombination(Integer.MAX_VALUE);
	}
	
	/**
	 * 	Busca todas as recomendações de combinações para elementos de origem de tamanho máximo definido.
	 * 
	 * @return Lista com todas combinações recomendadas
	 */
	public List<RecommendedAssociation> findAllRecommendedCombination(int itemsetMaxSize){
		
		logger.info("Finding recommendations for all itemsets of the specified mining result");
		
		List<RecommendedAssociation> result = new ArrayList<RecommendedAssociation>();
		
		ItemsetDAO itemsetDAO = new ItemsetDAO();
		List<Itemset> itemsets = itemsetDAO.findDistinctPremiseElements(miningResultId, itemsetMaxSize);
		logger.debug(String.format("Processing %d distinct premises", itemsets.size()));
		for (Itemset it : itemsets) {
			RecommendedAssociation rc = findRecommendedCombination(it.getElements());
			rc.setBasedOnPremise(true);
			rc.setBasedOnConsequence(false);
			result.add(rc);
		}
		
		logger.info(String.format("Process finished! %d recommendations finded", result.size()));
		return result;
	}

	private RecommendedAssociation findRecommendedCombination(Set<ApiMethod> fromMethods){
		
		logger.info(String.format("Finding recommendation from the methods %s ", fromMethods));
		RecommendedAssociation rc = new RecommendedAssociation();
		rc.setFromMethods(fromMethods);
		rc.setRecommendedSets(new ArrayList<RecommendedSet>());
		
		List<Rule> rules = ruleDAO.findByResultAndPremise(miningResultId, fromMethods);
		
		logger.debug("Making recommended sets");
		for (Rule r : rules) {
			RecommendedSet rs = new RecommendedSet();
			rs.setMethodSet(r.getConsequence().getElements());
			rs.setRuleBased(r.getSimpleRule());
			rs.setRecommendedCombination(rc);
			this.calculateFactor(rs);
			
			rc.getRecommendedSets().add(rs);
		}
		
		logger.debug(String.format("Ordering the recommended sets"));
		Collections.sort(rc.getRecommendedSets());
		
		return rc;
	}
	
	private void calculateFactor(RecommendedSet recommendedSet){
		int factorSum = 0;
		double valuesSum = 0;
		for (AbstractRecommenderCriteria<RecommendedSet> rc : criterias) {
			valuesSum += rc.calculateValue(recommendedSet) * rc.getFator().getFactor();
			factorSum += rc.getFator().getFactor();
		}
		double value = valuesSum / factorSum;
		recommendedSet.setRecommendedSetFactor(value);
	}
		
	/**
	 * 	Adiciona novo critério de qualiade para conjuntos recomendados
	 * 
	 * @param recommenderCriteria Critério
	 */
	public void addCriteria(AbstractRecommenderCriteria<RecommendedSet> recommenderCriteria){
		this.criterias.add(recommenderCriteria);
	}
	
	/**
	 * 	Remove um critério de qualidade adicionado anteriormente
	 * 
	 * @param recommenderCriteria Critério
	 */
	public void removeCriteria(AbstractRecommenderCriteria<RecommendedSet> recommenderCriteria){
		this.criterias.remove(recommenderCriteria);
	}

	/**
	 * Retorna os critérios de qualidade utilizados
	 * 
	 * @return Conjunto de {@link AbstractRecommenderCriteria} adicionados
	 */
	public Set<AbstractRecommenderCriteria<RecommendedSet>> getCriterias() {
		return new HashSet<AbstractRecommenderCriteria<RecommendedSet>>(this.criterias);
	}
	
}

