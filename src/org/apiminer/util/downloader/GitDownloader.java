package org.apiminer.util.downloader;

import java.io.File;

import org.apiminer.util.downloader.DownloaderFactory.Downloader;
import org.eclipse.jgit.api.Git;

class GitDownloader extends Downloader {

	@Override
	protected void specificDownload(File path, String url) throws Exception {
		Git.cloneRepository()
			.setDirectory(path)
			.setURI(url)
			.setCloneAllBranches(false)
			.call();
	}

}
