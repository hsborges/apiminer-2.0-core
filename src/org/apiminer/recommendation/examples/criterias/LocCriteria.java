package org.apiminer.recommendation.examples.criterias;

import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.ExampleMetric;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.CriteriaFactor;

/**
 * Classe concreta que implementa o critério de qualidade para número de linhas de código (LOC) 
 * de um exemplo. Neste contexto é importante salientar que exemplos podem incluir várias chamadas de métodos 
 * diferentes e também várias sementes, esta última por sua vez é sempre maior ou igual ao número 
 * de chamadas envolvidas.
 * 
 * <p>
 * 
 * Esta implementação leva em conta o tamanho ideal do exemplo que é dado pelas seguintes situações:
 * <ul>
 * 	<li> No melhor caso, todos as métodos envolvidos são procedimentos que não recebem função. 
 * 		 Neste caso o tamanho ideal do exemplo é dado pelo número de sementes do exemplo.
 * 			<pre>mim_prefered_size = num_seeds</pre>
 * 	<li> No pior caso esperado, todos os parâmetros necessários pelos métodos teriam de ser inicializados 
 * 		 antes das chamadas (considerando uma linha por parâmetro) e todos os métodos envolvidos
 * 		 seriam funções que teriam seu retorno utilizado uma vez nas linhas subsequêntes. Neste caso o valor 
 * 		 ideal seria dado pela seguinte equação:
 * 			<pre>max_prefered_size = num_parameters + num_seeds + num_functions_on_seeds</pre>
 * </ul>
 * 
 * <p>
 * Todos exemplos com número de linhas maior que o pior caso ideal, certamente incluem linhas que não 
 * são idealmente necessárias para a execução do método. Neste sentido quanto maior este valor, menor 
 * será seu peso em relação à métrica LOC.
 * 
 * <p>
 * Se LOC do exemplo estiver entre os valores minimo e máximo ideal, ele receberá um valor inicial ({@value #IN_PREFERED_SIZE}) 
 * mais um complemento de acordo de quão próximo o LOC do exemplo está do melhor caso. Caso não esteja entre os valores ideais o valor máximo obtido será dado pelo valor inicial menos um percentual referente a diferença entre o LOC atual e o máximo 
 * ideal. Caso o LOC seja 3X maior que o valor máximo este receberá peso igual a 0. Exemplo:
 * <pre>
 *se tamanho_minimo_ideal <= LOC <= tamanho_maximo_ideal
 *	retorna VALOR_MAXIMO - (COMPLEMENTO * DISTANCIA_DO_MIN_IDEAL)
 *senão
 *	se (LOC / tamanho_maximo_ideal) > 3 
 *		retorna 0
 * 	senão
 * 		retorna VALOR_INICIAL - (VALOR_INICIAL * DIFF/3)
 * </pre>
 * 
 * 
 * 
 * 
 * @see AbstractRecommenderCriteria
 * @author hudson
 *
 */
public class LocCriteria extends AbstractRecommenderCriteria<Example> {
	
	public static final int IN_PREFERED_SIZE = 8;

	@Override
	public CriteriaFactor getFator() {
		return CriteriaFactor.VERY_HIGH;
	}

	@Override
	protected double calculate(Example example) {
		
		int paramsCounter = 0;
		int returnsCounter = 0;
		for (ApiMethod apm : example.getApiMethods()) {
			paramsCounter += apm.getParametersType().size();
			if (apm.isFunction()) {
				returnsCounter ++;
			}
		}
		
		int minPreferedSize = example.getSeeds().size();
		int maxPreferedSize = paramsCounter + minPreferedSize + returnsCounter;
		
		assert minPreferedSize <= maxPreferedSize;
		
		//TODO Reimplementar métricas
		int length = example.getMetrics().get(ExampleMetric.LOC.name());
//		int length = example.getFormattedCodeExample().split("\n").length - 2;
		
		double startFactor = (MAX_VALUE * (IN_PREFERED_SIZE / 10));
		
		double result = Double.MIN_VALUE;
		if ((minPreferedSize <= length) && (length <= maxPreferedSize)) {
			result = MAX_VALUE - ((MAX_VALUE - startFactor) * ( (length - minPreferedSize) / (maxPreferedSize - minPreferedSize) ));
		}else{
			double diff = (double) length / maxPreferedSize;
			if (diff > 3) {
				result = 0;
			}else{
				result = startFactor - (startFactor*(diff/3));
			}
		}
		
		assert result >= 0 && result <= MAX_VALUE;
		
		return result;
		
	}

}
