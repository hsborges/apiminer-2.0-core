package org.apiminer.tasks.implementations;

import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.MiningDAO;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.mining.AssociationRulesMine;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

import weka.associations.AbstractAssociator;

public class MiningTask extends AbstractTask {

	private AssociationRulesMine associationRules;
	private Class<? extends AbstractAssociator> algorithm;

	public MiningTask(AssociationRulesMine associationRules,
			Class<? extends AbstractAssociator> algorithm) throws IllegalArgumentException {

		if (associationRules == null) {
			throw new IllegalArgumentException(
					"The algorithm must be not null");
		} else if (algorithm == null) {
			throw new IllegalArgumentException(
					"Algorithm class must be not null");
		}

		this.associationRules = associationRules;
		this.algorithm = algorithm;

		super.status = TaskStatus.WAITING;
	}

	@Override
	public void execute() {
		super.status = TaskStatus.RUNNING;
		notifyObservers(TaskStatus.RUNNING);

		try {
			MiningResult miningResult = this.associationRules.build(algorithm);
			new MiningDAO().persist(miningResult, DatabaseType.PRE_PROCESSING);
			super.result = TaskResult.SUCCESS;
		} catch (Throwable e) {
			super.result = TaskResult.FAILURE;
			super.result.setProblem(e);
		} finally {
			super.status = TaskStatus.FINISHED;
			notifyObservers(TaskStatus.FINISHED);
		}

	}

	@Override
	public String toString() {
		return "Task to mine association rules from source API";
	}

}
