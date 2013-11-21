package org.apiminer.util.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apiminer.util.downloader.DownloaderFactory.Downloader;
import org.apiminer.util.downloader.exceptions.DownloadException;


class CompressedDownloader extends Downloader {
	
	private static enum CompressedType {
		JAR, TAR, ZIP, BZIP2, GZIP, XZ
	}

	@Override
	protected void specificDownload(File path, String url) throws Exception {
		CompressedType compressedType = null;
		
		if (url.toLowerCase().endsWith(".bzip2")) {
			compressedType = CompressedType.BZIP2;
		}else if (url.toLowerCase().endsWith(".gzip")) {
			compressedType = CompressedType.GZIP;
		}else if (url.toLowerCase().endsWith(".xz")) {
			compressedType = CompressedType.XZ;
		}else if (url.toLowerCase().endsWith(".jar")) {
			compressedType = CompressedType.JAR;
		}else if (url.toLowerCase().endsWith(".tar")) {
			compressedType = CompressedType.TAR;
		}else if (url.toLowerCase().endsWith(".zip")) {
			compressedType = CompressedType.ZIP;
		}else{
			throw new DownloadException("File format not supported");
		}
		
		File dest = new File(path,"compressedFile");
		try {
			FileUtils.copyURLToFile(new URL(url), dest);
		} catch (Exception e) {
			throw new DownloadException(e);
		}
		
		switch (compressedType) {
		
		case ZIP:
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(dest);
				Enumeration<ZipArchiveEntry> elements = zipFile.getEntries();
				while(elements.hasMoreElements()){
					ZipArchiveEntry element = elements.nextElement();
					File file = new File(path,element.getName());
					if (element.isDirectory()) {
						file.mkdirs();
					}else{
						file.getParentFile().mkdirs();
						file.createNewFile();
						InputStream is = null;
						FileOutputStream fos = null;
						try {
							is = zipFile.getInputStream(element);
							fos = new FileOutputStream(file);
							int b = -1;
							while ((b = is.read()) != -1) {
								fos.write(b);
							}
						} catch (Exception e) {
							throw new DownloadException("Fail on unzip file",e);
						} finally {
							if (is != null) {
								try{ 
									is.close();
								}catch (Exception e) {}
							}
							if (fos != null) {
								try{ 
									fos.close();
								}catch (Exception e) {}
							}
						}
					}
					
				}
			} catch (Exception e) {
				throw new DownloadException(e);
			} finally {
				if (zipFile != null) {
					try {
						zipFile.close();
					} catch (IOException e) {}
				}
			}
			
			break;
			
		default:
			throw new DownloadException("File format not yet supported");
			
		}
		
		dest.delete();
	}
	
}
