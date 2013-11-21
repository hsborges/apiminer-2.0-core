package org.apiminer.recommendation.associations.criterias;

import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.entities.mining.RuleMetrics;
import org.apiminer.entities.mining.SimpleRule;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.CriteriaFactor;

/**
 * Classe que representa um critério de qualidade, baseado nos valores das regras obtidas, para ponderação
 * de conjuntos recomendados.
 * <p>
 * Pesos deste critério:
 * <ul>
 * 		<li>Suporte = {@value #SUPPORT_FACTOR}</li>
 * 		<li>Tamanho da consequência = {@value #CONSEQUENCE_SIZE_FACTOR}</li>
 * 		<li>Confiança = {@value #CONFIDENCE_FACTOR}</li>
 * 		<li>Jaccard = {@value #JACCARD_FACTOR}</li>
 * </ul>
 * </p> 
 * 
 * @see AbstractRecommenderCriteria
 * 
 * @author Hudson S. Borges
 *
 */
public class RuleCriteria extends AbstractRecommenderCriteria<RecommendedSet> {
	
	public static final int SUPPORT_FACTOR = 5;
	public static final int CONSEQUENCE_SIZE_FACTOR = 1;
	public static final int CONFIDENCE_FACTOR = 2;
	public static final int JACCARD_FACTOR = 2;
	
	@Override
	public double calculate(RecommendedSet object) {
		SimpleRule rb = object.getRuleBased();
		
		double v1 = SUPPORT_FACTOR * ( (double) (rb.getTotalSupport() - 1) / rb.getTotalSupport()  );
		double v2 = CONSEQUENCE_SIZE_FACTOR - ((rb.getConsequence().size() < 10) ? ((rb.getConsequence().size() - 1 ) * 0.1) : 1);
		double v3 = CONFIDENCE_FACTOR * rb.getMetrics().get(RuleMetrics.CONFIDENCE.name());
		double v4 = JACCARD_FACTOR * rb.getMetrics().get(RuleMetrics.JACCARD.name());
		
		return v1 + v2 + v3 + v4;
	}

	@Override
	public CriteriaFactor getFator() {
		return CriteriaFactor.VERY_HIGH;
	}

}
