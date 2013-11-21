package org.apiminer.util.downloader.exceptions;

public class DownloadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4774655378975184533L;

	public DownloadException() {
		super("Download process failed!");
	}

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(Throwable cause) {
		super(cause);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
