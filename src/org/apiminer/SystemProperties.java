package org.apiminer;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apiminer.daos.Database;

/**
 * Class responsible to storing the system properties
 * 
 * @author Hudson S. Borges
 *
 */
public final class SystemProperties {
	
	public static final String USER_PROPERTIES_FILE = "user.properties";
	public static final String DATABASE_PROPERTIES_FILE = "database.properties";
	
	public static File WORKING_DIRECTORY;
	public static Integer MAX_THREADS;
	
	public static Database DATABASE;
	
	private static final String MAX_THREADS_KEY = "max-threads";
	private static final String WORKING_DIRECTORY_KEY = "working-directory";
	
	private static final String DATABASE_NAME_KEY = "database.name";
	private static final String DATABASE_PORT_KEY = "database.port";
	private static final String DATABASE_ADDRESS_KEY = "database.address";
	private static final String DATABASE_EXAMPLES_SCHEMA_NAME_KEY = "database.examplesSchemaName";
	private static final String DATABASE_PRE_PROCESSING_SCHEMA_NAME_KEY = "database.preProcessingSchemaName";
	private static final String DATABASE_PASSWORD_KEY = "database.password";
	private static final String DATABASE_USERNAME_KEY = "database.username";
	private static final String DATABASE_DIALECT_KEY = "database.dialect";
	private static final String DATABASE_DRIVER_CLASS_KEY = "database.driverClass";
	private static final String DATABASE_AUTO_UPDATE_KEY = "database.autoUpdate";
	
	static {
		// Load the user properties
		try {
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Load the database properties
		try {
			loadDatabaseProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void load() throws IOException {
		// Load the properties from the file
		Properties properties = new Properties();
		properties.load(SystemProperties.class.getClassLoader().getResourceAsStream(USER_PROPERTIES_FILE));
		
		// Set the properties
		String workingDirectoryProperty = properties.getProperty(WORKING_DIRECTORY_KEY);
		String maxThreadsProperty = properties.getProperty(MAX_THREADS_KEY);
		
		// Configure the working directory
		try {
			WORKING_DIRECTORY = new File(workingDirectoryProperty);
		} catch (Exception e1) {
			WORKING_DIRECTORY = null;
		}
		
		// Configure the max number of threads
		try {
			MAX_THREADS = Integer.parseInt(maxThreadsProperty);
		} catch (NumberFormatException e) {
			MAX_THREADS = Runtime.getRuntime().availableProcessors() / 2;
		}
		
	}
	
	public static void loadDatabaseProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(SystemProperties.class.getClassLoader().getResourceAsStream(DATABASE_PROPERTIES_FILE));
		
		// Configure the database properties
		DATABASE = new Database();
		DATABASE.setName(properties.getProperty(DATABASE_NAME_KEY, null));
		DATABASE.setDriverClass(properties.getProperty(DATABASE_DRIVER_CLASS_KEY, null));
		DATABASE.setDialect(properties.getProperty(DATABASE_DIALECT_KEY, null));
		DATABASE.setUsername(properties.getProperty(DATABASE_USERNAME_KEY, null));
		DATABASE.setPassword(properties.getProperty(DATABASE_PASSWORD_KEY, null));
		DATABASE.setPreProcessingSchema(properties.getProperty(DATABASE_PRE_PROCESSING_SCHEMA_NAME_KEY, null));
		DATABASE.setExamplesSchema(properties.getProperty(DATABASE_EXAMPLES_SCHEMA_NAME_KEY, null));
		DATABASE.setAddress(properties.getProperty(DATABASE_ADDRESS_KEY, null));
		DATABASE.setPort(Integer.parseInt(properties.getProperty(DATABASE_PORT_KEY, "0")));
		DATABASE.setAutoUpdate(Boolean.parseBoolean(properties.getProperty(DATABASE_AUTO_UPDATE_KEY, "true")));
	}
	
}
