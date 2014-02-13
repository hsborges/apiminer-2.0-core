package org.apiminer.tasks;

import java.util.Observable;

/**
 * @author Hudson S. Borges
 *
 */
public abstract class AbstractTask extends Observable {

	private TaskResult result = null;
	private TaskStatus status = TaskStatus.WAITING;

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

	public void setResult(TaskResult result) {
		this.result = result;
		this.notifyObservers(this.result);
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
		this.notifyObservers(this.status);
	}
	
	public void setResult(Throwable throwable) {
		this.result = TaskResult.FAILURE;
		this.result.setProblem(throwable);
		this.notifyObservers(this.result);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (result == TaskResult.FAILURE) {
			result.getProblem().printStackTrace();
		}
	}
}
