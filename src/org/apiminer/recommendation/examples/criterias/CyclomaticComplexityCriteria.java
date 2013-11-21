package org.apiminer.recommendation.examples.criterias;

import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.ExampleMetric;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.CriteriaFactor;

/**
 *  Critério relacionado a complexidade ciclomática do exemplo. Na literatura, é dado que 
 *  alto nível de complexidade ciclomatica infere diretamente na compreenção e manutenção 
 *  de código. Neste sentido, o calculo e identificação de exemplos com menor MCC 
 *  são mais legíveis e melhores que com valores maiores.
 *  
 *  <p>
 *  Para o calculo deste critério é utilizada a complexidade ciclomática de MacCabe. Tal métrica
 *  calcula o número de caminhos linearmente independentes e através do seguinte calculo:
 *  
 *  <pre>MAC_CABE_COMPLEXITY = NUMBER_OF_POINTS_OF_DECISION + 1 </pre>
 * 
 * 
 * @author Hudson S. Borges
 *
 */
public class CyclomaticComplexityCriteria extends
		AbstractRecommenderCriteria<Example> {

	@Override
	public CriteriaFactor getFator() {
		return CriteriaFactor.LOW;
	}

	@Override
	protected double calculate(Example object) {
		int mcc = object.getMetrics().get(ExampleMetric.DECISION_STATEMENTS) + 1;
		return 0;
	}

}
