package org.apiminer.analyzer;

@SuppressWarnings("serial")
public class AnalyzerException extends Exception {

	public AnalyzerException() {
		this("A problem occurred during the analysis of the project.");
	}

	public AnalyzerException(String message) {
		super(message);
	}

	public AnalyzerException(Throwable cause) {
		super(cause);
	}

	public AnalyzerException(String message, Throwable cause) {
		super(message, cause);
	}

}
