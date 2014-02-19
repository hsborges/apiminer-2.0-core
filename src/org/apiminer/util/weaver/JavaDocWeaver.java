package org.apiminer.util.weaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JavaDocWeaver {
	
	private static void addScriptsAndStylesheets(String inputFile, String outputFile, String serverURL) throws IOException {
		Document jsoup = Jsoup.parse(new File(inputFile),"UTF-8");
		
		Element head = jsoup.getElementsByTag("head").first();
		
		Element syntaxTheme = head.appendElement("link")
				.attr("href", "http://alexgorbatchev.com/pub/sh/current/styles/shThemeDefault.css")
				.attr("rel", "stylesheet")
				.attr("type", "text/css");
		
		Element syntaxCore = head.appendElement("script")
			.attr("src", "http://alexgorbatchev.com/pub/sh/current/scripts/shCore.js");
		
		Element syntaxBrush = head.appendElement("script")
				.attr("src", "http://alexgorbatchev.com/pub/sh/current/scripts/shBrushJava.js");
		
		head.appendChild(syntaxTheme);
		head.appendChild(syntaxCore);
		head.appendChild(syntaxBrush);
		
		Element jquery = head.appendElement("script")
				.attr("src", "http://code.jquery.com/jquery-1.11.0.min.js");
			
		Element jqueryMigrate = head.appendElement("script")
				.attr("src", "http://code.jquery.com/jquery-migrate-1.2.1.min.js");
		
		Element jqueryUi = head.appendElement("script")
				.attr("src", "http://code.jquery.com/ui/1.10.3/jquery-ui.min.js");
			
		Element jqueryTheme = head.appendElement("link")
				.attr("href", "http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css")
				.attr("rel", "stylesheet")
				.attr("type", "text/css");	
		
		head.appendChild(jquery);
		head.appendChild(jqueryMigrate);
		head.appendChild(jqueryUi);
		head.appendChild(jqueryTheme);
		
		Element apiminerSearchScript = head.appendElement("script")
				.attr("src", serverURL + "/resources/js/apiminer-search.js");
			
		Element apiminerWeaverScript = head.appendElement("script")
				.attr("src", serverURL + "/resources/js/apiminer-weaver.js");
		
		Element apiminerOnloadScript = head.appendElement("script")
				.html("window.onload = function () { get_dialog(); instruments();};");
		
		head.appendChild(apiminerSearchScript);
		head.appendChild(apiminerWeaverScript);
		head.appendChild(apiminerOnloadScript);
		
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(jsoup.toString().getBytes());
		fos.close();
	}
	
	public static void main(String args[]) throws IOException{
		boolean showUsage = false;
		
		File inputDirectory = null;
		File outputDirectory = null;
		String serverURL = null;
		
		if (args.length != 3) {
			showUsage = true;
		} else {
			inputDirectory = new File(args[0]);
			outputDirectory = new File(args[1]);
			serverURL = args[2];
			
			if (!inputDirectory.exists() || !inputDirectory.isDirectory()) {
				showUsage = true;
			}
		}
		
		if (outputDirectory.exists()) {
			System.out.println("Output directory already exists");
			System.exit(0);
		}else if (showUsage) {
			System.out.println("Usage: [filesDirectory] [outputDirectory] [serverURL]");
			System.exit(0);
		}
		
		outputDirectory.mkdirs();

		Stack<File> files = new Stack<File>();
		files.add(inputDirectory);
		do{
			File inputFile = files.pop();
			if (inputFile.isDirectory()) {
				files.addAll(Arrays.asList(inputFile.listFiles()));
			}else{
				File outputFile = new File(outputDirectory, inputFile.getAbsolutePath().replaceFirst(inputDirectory.getAbsolutePath(), ""));
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
				if (inputFile.getName().toLowerCase().endsWith(".html")) {
					addScriptsAndStylesheets(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), serverURL);
				}else{
					copyFiles(inputFile, outputFile);
				}
			}
		}while(!files.isEmpty());


	}

	public static void copyFiles(File sourceFile, File destFile) throws IOException {
		FileInputStream fis = new FileInputStream(sourceFile);
		FileOutputStream fos = new FileOutputStream(destFile);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0){
			fos.write(buffer, 0, length);
		}

		fis.close();
		fos.flush();
		fos.close();
	}

}
