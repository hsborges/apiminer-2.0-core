package org.apiminer.util;

import java.io.File;
import java.util.StringTokenizer;

public class ConsoleUtil {
	
	public static void execCommand(String command, String path) throws Exception {
		StringTokenizer st = new StringTokenizer(command);
		String[] commandArray = new String[st.countTokens()];
		for (int i = 0; i < st.countTokens() && st.hasMoreTokens(); i++) {
			commandArray[i] = st.nextToken();
		}
		
		executeCommand(commandArray, path);
	}
	
	public static void executeCommand(String command) throws Exception {
		StringTokenizer st = new StringTokenizer(command);
		String[] commandArray = new String[st.countTokens()];
		for (int i = 0; i < st.countTokens() && st.hasMoreTokens(); i++) {
			commandArray[i] = st.nextToken();
		}
		
		executeCommand(commandArray);
	}
	
	public static void executeCommand(String[] command, String path) throws Exception {
		executeCommand(command, null, path);
	}
	
	public static void executeCommand(String[] command, String[] envp, String path) throws Exception {
		File file = null;
		if (path != null) {
			file = new File(path);
		}		
		
		Process process = Runtime.getRuntime().exec(command, envp, file);
		process.waitFor();
		
		if ( process == null || process.exitValue() != 0 ) {
			throw new Exception("The console execution finished with error!");
		}
	}
	
	public static void executeCommand(String[] command) throws Exception {
		executeCommand(command, null);
	}

}
