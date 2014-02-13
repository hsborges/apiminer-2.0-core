package org.apiminer.tasks.implementations;

import org.apiminer.mining.AssociationRulesMine;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;

import weka.associations.AbstractAssociator;

public class AssociationRulesMineTask extends AbstractTask {

	private AssociationRulesMine associationRules;
	private Class<? extends AbstractAssociator> algorithm;

	public AssociationRulesMineTask(Class<? extends AbstractAssociator> algorithm,
			int maxNumberOfElements,
			double minConfidenceValue,
			int minSupportValue) throws IllegalArgumentException {
		
		if (algorithm == null) {
			throw new IllegalArgumentException("Algorithm class must be not null");
		}

		this.associationRules = new AssociationRulesMine();
		this.associationRules.setMinConfidenceValue(minConfidenceValue);
		this.associationRules.setMinSupportValue(minSupportValue);
		this.associationRules.setMaxNumberOfElements(maxNumberOfElements);
		
		this.algorithm = algorithm;

		super.setStatus(TaskStatus.WAITING);
	}

	@Override
	public void execute() {
		super.setStatus(TaskStatus.RUNNING);

		try {
			this.associationRules.buildAndPersist(algorithm);
			super.setResult(TaskResult.SUCCESS);
		} catch (Throwable e) {
			super.setResult(e);
		} finally {
			super.setStatus(TaskStatus.FINISHED);
		}
	}

	@Override
	public String toString() {
		return "Task to mine association rules from source API";
	}

}
