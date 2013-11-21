package org.apiminer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apiminer.daos.Database;

/**
 * Classe responsável por armazenar as propriedades do sistema.
 * 
 * @author Hudson S. Borges
 *
 */
public final class SystemProperties {
	
	public static final File DATA_DIR = new File("data");
	public static final File PROPERTIES_FILE = new File(DATA_DIR, "user.properties");
	public static final File DATABASE_PROPERTIES_FILE = new File(DATA_DIR, "database.properties");
	
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
		// Carrega as propriedades do usuário
		try {
			load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Carrega as propriedades da base de dados
		try {
			loadDatabaseProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void save() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(PROPERTIES_FILE));
		properties.setProperty(WORKING_DIRECTORY_KEY, WORKING_DIRECTORY.getAbsolutePath());
		properties.setProperty(MAX_THREADS_KEY, Integer.toString(MAX_THREADS));
		properties.store(new FileOutputStream(PROPERTIES_FILE, false), null);
	}
	
	public static void load() throws IOException {
		// Verifica se o arquivo com as propriedades existe, se não existe ele é criado.
		if (!PROPERTIES_FILE.exists()) {
			PROPERTIES_FILE.getParentFile().mkdirs();
			PROPERTIES_FILE.createNewFile();
		}
		
		// Carrega as propriedades
		Properties properties = new Properties();
		properties.load(new FileReader(PROPERTIES_FILE));
		
		// Recupera as propriedades
		String workingDirectoryProperty = properties.getProperty(WORKING_DIRECTORY_KEY);
		String maxThreadsProperty = properties.getProperty(MAX_THREADS_KEY);
		
		// Configura o diretorio de trabalho
		try {
			WORKING_DIRECTORY = new File(workingDirectoryProperty);
		} catch (Exception e1) {
			WORKING_DIRECTORY = null;
		}
		
		// Configura o numero maximo de Threads
		try {
			MAX_THREADS = Integer.parseInt(maxThreadsProperty);
		} catch (NumberFormatException e) {
			MAX_THREADS = Runtime.getRuntime().availableProcessors() / 2;
		}
		
	}
	
	public static void saveDatabaseProperties() throws IOException {
		Properties properties = new Properties();
		properties.put(DATABASE_NAME_KEY, DATABASE.getName());
		properties.put(DATABASE_DRIVER_CLASS_KEY, DATABASE.getDriverClass());
		properties.put(DATABASE_DIALECT_KEY, DATABASE.getDialect());
		properties.put(DATABASE_USERNAME_KEY, DATABASE.getUsername());
		properties.put(DATABASE_PASSWORD_KEY, DATABASE.getPassword());
		properties.put(DATABASE_PRE_PROCESSING_SCHEMA_NAME_KEY, DATABASE.getPreProcessingSchema());
		properties.put(DATABASE_EXAMPLES_SCHEMA_NAME_KEY, DATABASE.getExamplesSchema());
		properties.put(DATABASE_ADDRESS_KEY, DATABASE.getAddress());
		properties.put(DATABASE_PORT_KEY, DATABASE.getPort().toString());
		properties.put(DATABASE_AUTO_UPDATE_KEY, Boolean.toString(DATABASE.isAutoUpdate()));
		properties.store(new FileOutputStream(DATABASE_PROPERTIES_FILE, false), null);
	}
	
	public static void loadDatabaseProperties() throws IOException {
		// Verifica se o arquivo com as propriedades existe, se não existe ele é criado.
		if (!DATABASE_PROPERTIES_FILE.exists()) {
			DATABASE_PROPERTIES_FILE.getParentFile().mkdirs();
			DATABASE_PROPERTIES_FILE.createNewFile();
		}
		
		Properties properties = new Properties();
		properties.load(new FileReader(DATABASE_PROPERTIES_FILE));
		
		// Configurando as propriedades
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
