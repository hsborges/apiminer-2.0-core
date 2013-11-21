package org.apiminer.tasks;

public enum TaskResult{
	
	FAILURE {
		@Override
		public String toString() {
			return "Failure";
		}
	}, 
	
	SUCCESS {
		@Override
		public String toString() {
			return "Success";
		}
	};
	
	private Throwable problem;
	
	@Override
	public abstract String toString();

	public Throwable getProblem() {
		return problem;
	}

	public void setProblem(Throwable problem) {
		this.problem = problem;
	}
	
}