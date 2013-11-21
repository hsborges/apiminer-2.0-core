package org.apiminer.recommendation;

/**
 *    Enumerator que enumera os fatores possíveis para serem utilizados pelos critérios de qualidade.
 *    Tais fatores são utilizados para calculos ponderados quando existe mais de um critério de qualidade
 * 
 * @author Hudson S. Borges
 *
 */
public enum CriteriaFactor {
	
	VERY_LOW(2), LOW(4), MEDIUM(6), HIGH(8), VERY_HIGH(10);
	
	int factor = 0;
	
	private CriteriaFactor(int factor) {
		this.factor = factor;
	}

	/**
	 * Retorna o peso definido para cada fator
	 * 
	 * @return O valor inteiro que representa o peso do critério
	 */
	public int getFactor() {
		return factor;
	}
	
}
