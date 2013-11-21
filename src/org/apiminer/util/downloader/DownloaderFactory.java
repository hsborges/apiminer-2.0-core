package org.apiminer.util.downloader;

import java.io.File;

import org.apiminer.util.downloader.exceptions.DownloadException;

public final class DownloaderFactory {
	
	private DownloaderFactory(){}
	
	public static final Downloader getGitDownloader() {
		return new GitDownloader();
	}
	
	public static final Downloader getCompressedDownloader() {
		return new CompressedDownloader();
	}
	
	public static final Downloader getMercurialDownloader() {
		return new MercurialDownloader();
	}
	
	public static final Downloader getSubversionDownloader() {
		return new SubversionDownloader();
	}
	
	public static abstract class Downloader {
		public final File download(String projectName, String url, String placeToDownload) throws DownloadException {
			if (placeToDownload == null) {
				throw new DownloadException("Place to download must be a directory");
			}	

			File workingpath = new File(placeToDownload);
			if (!(workingpath.exists() && workingpath.isDirectory())) {
				throw new DownloadException("Working directory not exist or not configured!");
			}
			File path = new File(workingpath, projectName);
			
			if ( path.exists() ) {
				throw new DownloadException("The client directory already exists in the working directory, delete before trying again!");
			}
			
			if ( path.mkdirs() ){
				try{
					specificDownload(path, url);
				}catch(Exception ex){
					throw new DownloadException(ex);
				}
							
				return path;
			}else{
				throw new DownloadException("Impossible to make the directories");
			}
		}
		
		protected abstract void specificDownload(File path, String url) throws Exception;
		
	}

}
