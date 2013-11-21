package org.apiminer.tasks.implementations;

import java.util.HashSet;
import java.util.Set;

import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.examples.ExampleRecommender;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

public class ExampleRecommenderTask extends AbstractTask {

	private RecommendedSet recommendedSet;
	
	private Set<AbstractRecommenderCriteria<Example>> criterias = new HashSet<AbstractRecommenderCriteria<Example>>();
	
	public ExampleRecommenderTask(final Set<AbstractRecommenderCriteria<Example>> criterias){
		super();
		this.recommendedSet = null;
		this.criterias = criterias;
	}
	
	public ExampleRecommenderTask(final RecommendedSet recommendedSet, final Set<AbstractRecommenderCriteria<Example>> criterias){
		super();
		this.recommendedSet = recommendedSet;
		this.criterias = criterias;
	}
	
	@Override
	public void execute() {
		super.status = TaskStatus.RUNNING;
		super.notifyObservers(TaskStatus.RUNNING);
		
		try {
			ExampleRecommender exampleRecommender = new ExampleRecommender();
			for (AbstractRecommenderCriteria<Example> criteria : criterias) {
				exampleRecommender.addCriteria(criteria);
			}
			
			if (recommendedSet != null) {
				exampleRecommender.buildAndPersist(recommendedSet);
			} else {
				exampleRecommender.buildAndPersist();
			}
			
			super.result = TaskResult.SUCCESS;
		} catch (Throwable e) {
			super.result = TaskResult.FAILURE;
			super.result.setProblem(e);
		} finally {
			this.status = TaskStatus.FINISHED;
			super.notifyObservers(TaskStatus.FINISHED);
		}
	}

	@Override
	public String toString() {
		return "API examples recommender";
	}
	
}
