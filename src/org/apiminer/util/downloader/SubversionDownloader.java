package org.apiminer.util.downloader;

import java.io.File;

import org.apiminer.util.downloader.DownloaderFactory.Downloader;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

class SubversionDownloader extends Downloader {

	@Override
	protected void specificDownload(File path, String url) throws Exception {
		SVNClientManager clientManager = SVNClientManager.newInstance();
		SVNUpdateClient updateClient = clientManager.getUpdateClient();
		updateClient.doCheckout(SVNURL.parseURIEncoded(url), path, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
	}

}
