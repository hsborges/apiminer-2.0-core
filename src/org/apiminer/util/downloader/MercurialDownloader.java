package org.apiminer.util.downloader;

import java.io.File;

import org.apiminer.util.downloader.DownloaderFactory.Downloader;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CloneCommand;

class MercurialDownloader extends Downloader {

	@Override
	protected void specificDownload(File path, String url) throws Exception {
		new CloneCommand(Repository.clone(path, url)).execute(path.getAbsolutePath());
	}

}
