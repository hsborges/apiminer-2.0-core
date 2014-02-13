package org.apiminer.recommendation;

/**
 * 
 * Esta interface deve ser implementada por qualquer critério de qualidade para recomendações de conjuntos ou exemplos
 * 
 * @author Hudson S. Borges
 * @param <T>
 */
public interface IRecommenderCriteria<T> {
	
	/**
	 * @return Peso do critério
	 */
	CriteriaFactor getFator();
	
	/**
	 * 	Método que calcula o valor do objeto <T> 
	 * 
	 * @return O valor do objeto <T> de acordo com o critério 
	 */
	double calculateValue(T object);

}
