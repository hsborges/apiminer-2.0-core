package org.apiminer.recommendation.examples.criterias;

import org.apiminer.entities.example.Example;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.CriteriaFactor;

/**
 * Classe que implementa um critério de qualidade relacionado à relação entre o número de métodos envolvidos
 * e o número de sementes do exemplo, ressaltando que o número de sementes sempre será maior ou igual ao
 * número de métodos envolvidos.
 * 
 * <p>
 * A proporção ideal é de 1:1, onde existe uma semente para um método envolvido.<br>
 * Neste sentido o calculo da complexidade para este critério é dado como:
 * 		<pre>factor = MAX_VALUE * (num_seeds / num_methods)</pre>
 *
 * @see AbstractRecommenderCriteria
 * @author Hudson S. Borges
 *
 */
public class SeedsCriteria extends AbstractRecommenderCriteria<Example> {

	@Override
	public CriteriaFactor getFator() {
		return CriteriaFactor.LOW;
	}

	@Override
	protected double calculate(Example example) {
		double result = MAX_VALUE * ((double) example.getApiMethods().size() / example.getSeeds().size());
		assert result >= 0 && result <= MAX_VALUE;
		return result;
	}

}
