package org.apiminer.recommendation.associations.criterias;

import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.CriteriaFactor;

/**
 * 	Este critério calcula o peso do conjunto recomendado baseado nas dependências estruturais existentes 
 * 	entre os elementos de origem e os recomendados.
 * 
 *  <p>
 *  Inicialmente, todos os conjuntos possuem 50% do valor máximo do critério e para cada elemento de origem 
 *  é verificado se este possui algum dependência com os elementos recomendados. Caso exista é adicionado 
 *  um valor de x% do valor máximo ao valor inicial, caso contrário será subtraido os mesmos x% do valor inicial.
 *  </p>
 *  
 *  <p>
 *  O valor de x é dado por: <br/>
 *  	&nbsp &nbsp &nbsp (MAX_VALUE / 2) / NUMBER_OF_ELEMENTS_IN_ORIGIN
 *  </p>
 *  
 *  <p>
 *  Tipos de dependências, dado um elemento de origem:
 *  <ul>
 *  	<li>Algum elemento recomendado utiliza como parâmetro</li>
 *  	<li>Algum elemento recomendado é do tipo retornado pelo elemento de origem</li>
 *  </ul>
 *  </p>
 * 
 * @see AbstractRecommenderCriteria
 * @author Hudson S. Borges
 *
 */
public class DataDependencyCriteria extends AbstractRecommenderCriteria<RecommendedSet> {
	
	@Override
	public CriteriaFactor getFator() {
		return CriteriaFactor.MEDIUM;
	}

	@Override
	public double calculate(RecommendedSet object) {
		double subFactor = ((double) MAX_VALUE / 2) / object.getRecommendedCombination().getFromMethods().size();
		double value = 5;
		for (ApiMethod apm : object.getRecommendedCombination().getFromMethods()) {
			boolean hasDepencency = false;
			for (ApiMethod apm2 : object.getMethodSet()) {
				if (apm2.getParametersType().contains(apm.getReturnType())) {
					hasDepencency = true;
					break;
				}else if (apm2.getApiClass().getName().equals(apm.getReturnType())) {
					hasDepencency = true;
					break;
				}
			}
			if (hasDepencency) {
				value += subFactor;
			}
		}
		return value;
	}

}
