package org.apiminer.builder.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import org.apiminer.SystemProperties;
import org.apiminer.builder.IBuilder;
import org.apiminer.util.ConsoleUtil;
import org.apiminer.util.FilesUtil;


/**
 * Builder to android projects.
 * 
 * @author Hudson S. Borges
 *
 */
public class AndroidBuilder implements IBuilder {

	private static final File PROPERTIES_FILE = new File(SystemProperties.DATA_DIR,"android-builder.properties"); 
	
	private Properties properties = new Properties();
	
	{
		
		InputStream is = null;
		try{
			is = new FileInputStream(PROPERTIES_FILE);
			properties.load(is);
		} catch (Exception e) {
			FileOutputStream fos = null;
			try {
				if (!PROPERTIES_FILE.exists()) {
					PROPERTIES_FILE.createNewFile();
				}
				fos = new FileOutputStream(PROPERTIES_FILE);
				properties.store(fos,null);
			} catch (Exception e1) {
				//TODO Tratar possivel erro
			} finally {
				try{
					if (fos != null){
						fos.close();
					}
				}catch(Exception e1){}
			}
		}finally{
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * build the manifest files to generate the R.java file
	 * 
	 * @param manifestFiles
	 *            list of manifest files that will be build
	 * @return true if it build correctly, false otherwise
	 */
	private boolean buildAapt(ArrayList<String> manifestFiles) {

		// aapt package -v -f -m -S res -J src -M ./AndroidManifest.xml -I
		// /Developer/Android/android-sdk/platforms/android-Honeycomb/android.jar
		if (manifestFiles.size() == 0) {
			return true;
		}

		boolean sucess = true;

		for (String manifest : manifestFiles) {

			String currentDir = new File(manifest).getParent();

			if (currentDir.toLowerCase().matches(
					".*" + File.separator + "test[s]?[" + File.separator
							+ "]?.*")) {
				continue;
			}

			try {

				String[] commandArray = new String[] {
						properties.getProperty("android-aapt"), "package", // Mode
																				// of
																				// compilation
						// "-v", // verbose output
						// "-f", // force overwrite of existing files
						"-m", // make package directories under location
								// specified by -J
						"-S", // directory in which to find resources. Multiple
								// directories will be scanned
								// and the first match found (left to right)
								// will take precedence.
						currentDir.concat(File.separator).concat("res"), "-J", // specify
																				// where
																				// to
																				// output
																				// R.java
																				// resource
																				// constant
																				// definitions
						currentDir.concat(File.separator).concat("src"), "-M", // specify
																				// full
																				// path
																				// to
																				// AndroidManifest.xml
																				// to
																				// include
																				// in
																				// zip
						manifest, "-I", // add an existing package to base
										// include set
						properties.getProperty("android-jar-path") };
 
				ConsoleUtil.executeCommand(commandArray, currentDir);

			} catch (Exception e) {
				sucess = false;
			}

		}

		return sucess;

	}

	/**
	 * Generate the java files from aidl files
	 * 
	 * @param aidlFiles
	 *            list of aidl files that will be generated
	 * @return true if it builds correctly, false otherwise
	 */
	private boolean buildAidl(ArrayList<String> aidlFiles) {

		if (aidlFiles.size() == 0) {
			return true;
		}

		boolean sucess = true;
		for (String aidlFile : aidlFiles) {

			if (aidlFile.lastIndexOf("src") != -1) {

				String currentDir = aidlFile.substring(
						0,
						aidlFile.lastIndexOf("src"));
				try {

					// String command = new String(AIDL + " -I" + currentDir +
					// "src" + File.separator + " " + aidlFile);

					String[] commandArray = new String[] {
							properties.getProperty("android-aidl"),
							aidlFile };

					ConsoleUtil.executeCommand(commandArray, currentDir);

				} catch (Exception e) {
					sucess = false;
				}

			}
		}

		return sucess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.br.ufmg.apiminer.build.IBuild#build(java.lang.String)
	 */
	@Override
	public boolean build(String path) {
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			if (entry.getValue() == null) {
				throw new RuntimeException("Property "+entry.getKey()+" not configured on file "+PROPERTIES_FILE);
			}else{
				File f = new File(entry.getValue().toString());
				if (!f.exists()) {
					throw new RuntimeException("Property "+entry.getKey()+" not configured on file "+PROPERTIES_FILE);
				}
			}
		}

		ArrayList<String> aidlFiles = FilesUtil.collectFiles(path, ".aidl");
		ArrayList<String> manifestFiles = FilesUtil.collectFiles(
				path,
				"AndroidManifest.xml");

		boolean aidlCompleted = this.buildAidl(aidlFiles);
		boolean aaptCompleted = this.buildAapt(manifestFiles);

		return aidlCompleted && aaptCompleted;
	}

	@Override
	public String getBuilderName() {
		return "Android AAPT and AIDL Builder";
	}

}
