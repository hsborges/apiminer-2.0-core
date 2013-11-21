package org.apiminer.util.git;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apiminer.entities.api.GitHubRepository;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class RepositoryUtil {
	
	@Deprecated
	public void exportToXML(final String filePath, List<GitHubRepository> repositories, final Integer repositoriesPerFile) throws IOException {
		File pathFile = new File(filePath);
		if (!(pathFile.exists() && pathFile.isDirectory())) {
			throw new IllegalArgumentException("Export directory does not exist");
		}

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
		Date thisDate = new Date(System.currentTimeMillis());

		File file = new File(pathFile, format.format(thisDate));
		file.mkdirs();
		if (repositoriesPerFile == null) {
			saveToXML(repositories, file.getAbsolutePath());
		} else {
			saveToXML(repositories, file.getAbsolutePath(), repositoriesPerFile);
		}
	}

	@Deprecated
	public void exportToXML(String path, List<GitHubRepository> repositories) throws IOException {
		exportToXML(path, repositories, null);
	}

	@Deprecated
	public void saveToXML(List<GitHubRepository> repositories, String parent, int numberPerFile) throws IOException {
		XStream stream = new XStream(new DomDriver());

		int counter = 1;

		int init = 0;
		int pointer = numberPerFile - 1;

		if (repositories.size() <= numberPerFile) {
			File file = new File(parent, "repositories.xml");
			FileOutputStream fos = new FileOutputStream(file);
			stream.toXML(new ArrayList<GitHubRepository>(repositories), fos);
			fos.close();
		} else {
			while (pointer < repositories.size()) {
				File file = new File(parent, "repositories(piece" + (counter++) + ").xml");
				FileOutputStream fos = new FileOutputStream(file);
				List<GitHubRepository> listToSave = new ArrayList<GitHubRepository>(repositories.subList(init, pointer));
				stream.toXML(listToSave, fos);
				init += numberPerFile;
				pointer += numberPerFile;
				if (init < repositories.size()
						&& pointer >= repositories.size()) {
					pointer = repositories.size() - 1;
				}
				fos.close();
			}
		}
	}

	@Deprecated
	public void saveToXML(List<GitHubRepository> repositories, String parent) throws IOException {
		File file = new File(parent, String.format("repositories(%s).xml",SimpleDateFormat.getDateTimeInstance().format(new Date())));
		FileOutputStream fos = new FileOutputStream(file);

		XStream stream = new XStream(new DomDriver());
		stream.toXML(new ArrayList<GitHubRepository>(repositories), fos);

		fos.close();
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public static List<GitHubRepository> convertXMLtoGitHubRepositories(String xmlFile) throws Exception {
		if (xmlFile == null) {
			throw new NullPointerException("Invalid XML File! Parameter null.");
		}

		File f = new File(xmlFile);

		if (!f.exists() || !f.isFile()) {
			throw new IllegalArgumentException("File not found!");
		}

		try {
			List<GitHubRepository> repositories = (List<GitHubRepository>) new XStream(
					new DomDriver()).fromXML(new FileInputStream(xmlFile));
			return repositories;
		} catch (Exception ex) {
			throw ex;
		}
	}

}
