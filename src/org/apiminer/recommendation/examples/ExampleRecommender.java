package org.apiminer.recommendation.examples;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.RecommendedSetDAO;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.recommendation.AbstractRecommenderCriteria;

/**
 * 	Classe responsável por fazer a ligação entre as combinações recomendadas e os 
 * exemplos disponíveis na base de dados. Os exemplos são ordenados de acordo com critérios de
 * qualidade fornecidos.
 * 
 * @author Hudson S. Borges
 *
 */
public class ExampleRecommender {
	
	private static final Logger LOGGER = Logger.getLogger(ExampleRecommender.class);
	
	private final Set<AbstractRecommenderCriteria<Example>> criterias = new HashSet<AbstractRecommenderCriteria<Example>>();
	
	private Map<Example, Double> mapAux;
	
	/**
	 *  Método que itera sobre todos os conjuntos recommendados e para cada um busca pelos exemplos relacionados 
	 *  e os ordena seguindos os critérios de qualidade fornecidos.
	 *  
	 *  <p>Os resultados são persistidos ao final da análise de cada recomendação analisada.
	 */
	public void buildAndPersist(){
		RecommendedSetDAO recommendedSetDAO = new RecommendedSetDAO();
		LOGGER.info("Building all recommended examples for each recommended set");
		int count = 0;
		List<RecommendedSet> result;
		while (!(result = recommendedSetDAO.findAllRecommendedSets(count++, 100)).isEmpty()) {
			for (RecommendedSet recommendedSet : result) {
				//TODO Avaliar a possibilidade de executar com threads
				buildAndPersist(recommendedSet);
			}
		}
	}
	
	public void buildAndPersist(RecommendedSet recommendedSet) {
		Set<ApiMethod> fromMethods = new HashSet<ApiMethod>(recommendedSet.getMethodSet());
		fromMethods.addAll(recommendedSet.getRecommendedCombination().getFromMethods());
		
		List<Example> examples = new ExampleDAO().findExamplesWithMethods(fromMethods);
		
		this.mapAux = new HashMap<Example, Double>();
		Collections.sort(examples, new Comparator<Example>() {
			@Override
			public int compare(Example ex1, Example ex2) {
				double value1 = getValue(ex1);
				double value2 = getValue(ex2);
				if (value1 == value2) {
					return 0;
				} else if (value1 > value2) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		
		recommendedSet.setRecommendedExamples(examples);
		new RecommendedSetDAO().update(recommendedSet);
	}
	
	/**
	 * Adiciona novo critério para aferição de qualidade entre exemplos de código
	 * 
	 * @param criteria 
	 */
	public void addCriteria(AbstractRecommenderCriteria<Example> criteria){
		this.criterias.add(criteria);
	}
	
	
	/**
	 * Remove um critério adicionado anteriormente.
	 * 
	 * @param criteria Critério a ser removido
	 * @return <code>true</code> se o critério foi removido com sucesso e <code>false</code> caso contrário.
	 */
	public boolean removeCriteria(AbstractRecommenderCriteria<Example> criteria){
		return this.criterias.remove(criteria);
	}
	
	private double getValue(Example example){
		if (mapAux.containsKey(example)) {
			return mapAux.get(example);
		}else{
			int factorSum = 0;
			double valuesSum = 0;
			for (AbstractRecommenderCriteria<Example> rc : criterias) {
				valuesSum += rc.calculateValue(example) * rc.getFator().getFactor();
				factorSum += rc.getFator().getFactor();
			}
			
			double value = (valuesSum / factorSum) - example.getProblems().size();
			mapAux.put(example, value);
			return value;
		}
	}
	
}
