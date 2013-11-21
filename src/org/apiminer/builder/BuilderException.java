package org.apiminer.builder;

public class BuilderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5434710314422955614L;

	public BuilderException() {
		super("Fail on build project");
	}

	public BuilderException(String message) {
		super(message);
	}

	public BuilderException(Throwable cause) {
		super(cause);
	}

	public BuilderException(String message, Throwable cause) {
		super(message, cause);
	}

}
