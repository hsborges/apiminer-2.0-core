package org.apiminer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FilesUtil {
	
	public static final String readFile(String filePath) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filePath)));
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		while ( (line = bufferedReader.readLine()) != null ) {
			builder.append(line).append("\n");
		}
		
		bufferedReader.close();
		return builder.toString().trim();
	}
	
	public static final Collection<String> collectFiles(String initialDirectory, String postFix, boolean ignoreCase) {
		ArrayList<String> files = new ArrayList<String>();
		File f = new File(initialDirectory);

		boolean isSearchedFile = f.isFile();
		if (ignoreCase) {
			isSearchedFile = isSearchedFile && f.getName().toLowerCase().endsWith(postFix.toLowerCase());
		}else{
			isSearchedFile = isSearchedFile && f.getName().endsWith(postFix);
		}
		
		if (isSearchedFile) {
			files.add(f.getAbsolutePath());
		} else if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				files.addAll(collectFiles(child.getAbsolutePath(), postFix));
			}
		}
		
		return files;
	}
	
	public static final ArrayList<String> collectFiles(String initialDirectory, String postFix) {
		ArrayList<String> files = new ArrayList<String>();
		File f = new File(initialDirectory);

		if (f.isFile() && f.getName().endsWith(postFix)) {
			files.add(f.getAbsolutePath());
		} else if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				files.addAll(collectFiles(child.getAbsolutePath(), postFix));
			}
		}

		return files;
	}
	
	public static final ArrayList<String> collectDirectories(String initialDirectory, String dirName) {
		ArrayList<String> files = new ArrayList<String>();
		File f = new File(initialDirectory);

		if (f.isDirectory()) {
			if (f.getName().equals(dirName)) {
				files.add(f.getAbsolutePath());
			}
			for (File child : f.listFiles()) {
				files.addAll(collectDirectories(child.getAbsolutePath(), dirName));
			}
		}

		return files;
	}
	
	public static void deleteFile(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					deleteFile(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}
 
	public static File createTempDir(String dirName) throws IOException {
		File dir = new File(System.getProperty("java.io.tmpdir"), dirName);
		dir.mkdir();
		return dir;
	}

}
