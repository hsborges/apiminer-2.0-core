package org.apiminer.recommendation;

/**
 * 	Classe abstrata que deve ser estendida por qualquer critério de qualidade para recomendações
 * 
 * @author Hudson S. Borges
 * @param <T>
 */
public abstract class AbstractRecommenderCriteria<T> implements IRecommenderCriteria<T> {

	
	/**
	 *  Valor máximo do critério
	 */
	public static final int MAX_VALUE = 10;

	/**
	 * @return Um valor entre 0 e {@value #MAX_VALUE}
	 */
	@Override
	public final double calculateValue(T object){
		double value = calculate(object);
		if (value > MAX_VALUE || value < 0) {
			throw new RuntimeException("Criteria not valid, please re-implement the criteria"); 
		}else{ 
			return value;
		}
	}
	
	
	/**
	 * @param object
	 * @return
	 */
	protected abstract double calculate(T object);


	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
