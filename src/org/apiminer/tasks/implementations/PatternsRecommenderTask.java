package org.apiminer.tasks.implementations;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apiminer.daos.RecommendedCombinationDAO;
import org.apiminer.entities.example.RecommendedAssociation;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.recommendation.AbstractRecommenderCriteria;
import org.apiminer.recommendation.associations.CombinationRecommender;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

public class PatternsRecommenderTask extends AbstractTask {
	
	private MiningResult miningResult;
	private List<AbstractRecommenderCriteria<RecommendedSet>> criterias;
	private int itemsetMaxSize;
	
	public PatternsRecommenderTask(MiningResult miningResult,
			List<AbstractRecommenderCriteria<RecommendedSet>> criterias,
			int itemsetMaxSize) {
		super();
		this.miningResult = miningResult;
		this.criterias = criterias;
		this.itemsetMaxSize = itemsetMaxSize;
	}

	@Override
	public void execute() {
		super.status  = TaskStatus.RUNNING;
		super.notifyObservers(TaskStatus.RUNNING);
		
		CombinationRecommender recommender = new CombinationRecommender(miningResult.getId());
		for (AbstractRecommenderCriteria<RecommendedSet> criteria : criterias) {
			recommender.addCriteria(criteria);
		}

		try {
			List<RecommendedAssociation> all = recommender.findAllRecommendedCombination(itemsetMaxSize);
			new RecommendedCombinationDAO().persistOnBatch(all);
			super.result = TaskResult.SUCCESS;
		} catch (Exception e) {
			super.result = TaskResult.FAILURE;
			super.result.setProblem(e);
		} finally {
			super.status = TaskStatus.FINISHED;
			super.notifyObservers(TaskStatus.FINISHED);
		}
		
	}

	@Override
	public String toString() {
		return String.format("Task to derive recommendations of patterns mined on %s.", SimpleDateFormat.getDateTimeInstance().format(miningResult.getAddedAt()));
	}

}
