package org.apiminer.tasks;

import java.util.Observable;

public abstract class AbstractTask extends Observable {

	protected TaskResult result = null;
	protected TaskStatus status = TaskStatus.WAITING;

	public abstract void execute();
	
	public abstract String toString();

	@Override
	public void notifyObservers(Object arg) {
		if (countObservers() > 0) {
			setChanged();
			super.notifyObservers(arg);
		}
	}

	public TaskResult getResult() {
		return result;
	}

	public TaskStatus getStatus() {
		return status;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (result == TaskResult.FAILURE) {
			result.getProblem().printStackTrace();
		}
	}

}
