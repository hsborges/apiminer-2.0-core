package org.apiminer.builder.implementations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apiminer.builder.BuilderException;
import org.apiminer.builder.IBuilder;
import org.apiminer.util.ConsoleUtil;
import org.apiminer.util.FilesUtil;

/**
 * Builder of maven projects.
 * 
 * @author Hudson S. Borges
 *
 */
public class MavenBuilder implements IBuilder {
	
	private static final String MVN_COMMAND = "mvn";
	private static final String MVN_ACTION = "dependency:copy-dependencies";
	
	private String[] getCommand(String outputDir){
		return new String[]{MVN_COMMAND, MVN_ACTION, "-DoutputDirectory=".concat(outputDir)};
	}

	@Override
	public boolean build(String path) throws BuilderException {
		File pomXML = new File(path);
		ArrayList<String> mavenFiles = new ArrayList<String>();
		if (!pomXML.exists() || !pomXML.isDirectory()) {
			throw new IllegalArgumentException("Directory not exist!");
		}else{
			mavenFiles.addAll(FilesUtil.collectFiles(path, "pom.xml"));
		}
		
		List<Exception> exceptions = new ArrayList<Exception>();
		if (mavenFiles.isEmpty()) {
			return true;
		}else{
			for (String file : mavenFiles) {
				File inputFileDir = new File(file).getParentFile();
				File outputDir = new File(inputFileDir, "mvnDependencies");
				try {
					ConsoleUtil.executeCommand(getCommand(outputDir.getAbsolutePath()),new String[]{"MAVEN_OPTS=-Xmx128m"}, inputFileDir.getAbsolutePath());
				} catch (Exception e) {
					exceptions.add(e);
				}
			}
			if (!exceptions.isEmpty()) {
				return false;
			}else{
				return true;
			}
		}
	}

	@Override
	public String getBuilderName() {
		return "Maven Dependency Downloader";
	}

}
