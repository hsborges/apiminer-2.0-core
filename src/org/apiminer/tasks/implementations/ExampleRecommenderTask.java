package org.apiminer.tasks.implementations;

import java.util.List;

import org.apiminer.daos.ProjectDAO;
import org.apiminer.daos.AssociatedElementDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.recommendation.examples.ExampleRecommender;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

public class ExampleRecommenderTask extends AbstractTask {

	public ExampleRecommenderTask(){
		super();
	}
	
	@Override
	public void execute() {
		super.setStatus(TaskStatus.RUNNING);
		
		try {
			ExampleRecommender exampleRecommender = new ExampleRecommender();
			
			List<AssociatedElement> res = new AssociatedElementDAO().findAllAssociatedElements();
			for (AssociatedElement re : res) {
				exampleRecommender.makeRecommendations(re);
			}
			
			for (ApiClass apc : new ProjectDAO().findSourceAPI().getApiClass()) {
				for (ApiMethod apm : apc.getApiMethods()) {
					exampleRecommender.makeRecommendations(apm);
				}
			}
			
			super.setResult(TaskResult.SUCCESS);
		} catch (Throwable e) {
			super.setResult(e);
		} finally {
			this.setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public String toString() {
		return "API examples recommender";
	}
	
}
