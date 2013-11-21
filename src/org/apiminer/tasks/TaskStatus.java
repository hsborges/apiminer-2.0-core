package org.apiminer.tasks;

public enum TaskStatus {
	
	FINISHED {
		@Override
		public String toString() {
			return "Finished";
		}
	},
	RUNNING {
		@Override
		public String toString() {
			return "Running";
		}
	},
	WAITING
	{
		@Override
		public String toString() {
			return "Waiting";
		}
	};
	
	@Override
	public abstract String toString();
	
}
