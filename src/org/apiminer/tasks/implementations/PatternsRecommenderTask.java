package org.apiminer.tasks.implementations;

import java.text.SimpleDateFormat;

import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.GenericDAO;
import org.apiminer.entities.example.Recommendation;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.recommendation.associations.AssociationsRecommender;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

public class PatternsRecommenderTask extends AbstractTask {
	
	private MiningResult miningResult;
	
	public PatternsRecommenderTask(MiningResult miningResult) {
		super();
		this.miningResult = miningResult;
	}

	@Override
	public void execute() {
		super.setStatus(TaskStatus.RUNNING);
		
		AssociationsRecommender recommender = new AssociationsRecommender(miningResult);

		try {
			GenericDAO dao = new GenericDAO() {
				@Override
				public Class<?> getObjectType() {
					return Recommendation.class;
				}
			};
			
			for (Recommendation recomendation : recommender.findAllRecommendedAssociations()) {
				dao.persist(recomendation, DatabaseType.EXAMPLES);
			}
			
			super.setResult(TaskResult.SUCCESS);
		} catch (Exception e) {
			super.setResult(e);
		} finally {
			super.setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public String toString() {
		return String.format("Task to derive recommendations of patterns mined on %s.", SimpleDateFormat.getDateTimeInstance().format(miningResult.getAddedAt()));
	}

}
