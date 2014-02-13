package org.apiminer.util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apiminer.daos.Database;
import org.apiminer.daos.DatabaseType;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.config.TargetDatabase;
import org.eclipse.persistence.logging.SessionLog;

public class DatabaseUtil {
	
	/**
	 * Pre-processing entity manager factory
	 */
	private static EntityManagerFactory PP_FACTORY = null;
	
	/**
	 * Examples entity manager factory
	 */
	private static EntityManagerFactory E_FACTORY = null;
	
	/**
	 * Replicated (Pre-processing and Examples) entity manager factory 
	 */
	private static EntityManagerFactory R_FACTORY = null;
	
	/**
	 *  Connected database
	 */
	private static Database DATABASE = null;
	
	/**
	 * Singleton pattern
	 */
	private DatabaseUtil(){};
	
	private static synchronized void buildReplicatedEntityManagerFactory() {
		if (DATABASE == null || !DATABASE.isValid()) {
			throw new PersistenceException("Database properties not configured!");
		}
		
		Map<String, Object> properties = getProperties(DATABASE);
		properties.put(PersistenceUnitProperties.JDBC_URL, DATABASE.getPreProcessingUrl());
		
		properties.put(PersistenceUnitProperties.PARTITIONING, "Replicate");
		
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.driver", DATABASE.getDriverClass());
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.url", DATABASE.getExamplesUrl());
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.user", DATABASE.getUsername());
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.password", DATABASE.getPassword());
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.initial", "1");
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.min", "64");
		properties.put(PersistenceUnitProperties.CONNECTION_POOL+"node2.max", "64");

		R_FACTORY = Persistence.createEntityManagerFactory("replicated", properties);
	}
	
	public static synchronized EntityManager getEntityManager(Database database, DatabaseType databaseType){
		if(DATABASE == null || !DATABASE.equals(database)) {
			connect(database);
		}
		
		switch(databaseType){
		
		case EXAMPLES: {
			return E_FACTORY.createEntityManager();
		}

		case PRE_PROCESSING: {
			return PP_FACTORY.createEntityManager();
		}
		
		case REPLICATED: {
			return R_FACTORY.createEntityManager();
		}
		
		default:
			return null;
			
		}
	}
	
	private static void connect(Database database){
		if (!database.isValid()) {
			throw new PersistenceException("Invalid database properties!");
		} else {
			closeConnections();
		}
		
		Map<String, Object> properties = getProperties(database);
		properties.put("javax.persistence.jdbc.url", database.getExamplesUrl());
		DatabaseUtil.E_FACTORY = Persistence.createEntityManagerFactory("examples", properties);
		
		properties = getProperties(database);
		properties.put("javax.persistence.jdbc.url", database.getPreProcessingUrl());
		DatabaseUtil.PP_FACTORY = Persistence.createEntityManagerFactory("pre-processing", properties);
		
		DatabaseUtil.DATABASE = database;
		
		buildReplicatedEntityManagerFactory();
	}

	private static Map<String, Object> getProperties(Database database) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PersistenceUnitProperties.JDBC_DRIVER, database.getDriverClass());
		properties.put(PersistenceUnitProperties.JDBC_USER, database.getUsername());
		properties.put(PersistenceUnitProperties.JDBC_PASSWORD, database.getPassword());
		
		properties.put(PersistenceUnitProperties.CONNECTION_POOL_INITIAL, "1");
		properties.put(PersistenceUnitProperties.CONNECTION_POOL_MIN, "64");
		properties.put(PersistenceUnitProperties.CONNECTION_POOL_MAX, "64");
		
		properties.put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_DATABASE_GENERATION);
		properties.put(PersistenceUnitProperties.TARGET_DATABASE, TargetDatabase.Auto);
		properties.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.SEVERE_LABEL);
		
		properties.put(PersistenceUnitProperties.BATCH_WRITING, "JDBC");
		properties.put(PersistenceUnitProperties.CACHE_SIZE_DEFAULT, "1000");
		properties.put(PersistenceUnitProperties.PERSISTENCE_CONTEXT_FLUSH_MODE, FlushModeType.COMMIT.name());
		
		if (database.isAutoUpdate()) {
			properties.put("eclipselink.ddl-generation", PersistenceUnitProperties.CREATE_OR_EXTEND);
		}
		return properties;
	}

	public static Database getDatabase() {
		return DATABASE;
	}

	public static synchronized void closeConnections() {
		if (PP_FACTORY != null && PP_FACTORY.isOpen()) {
			PP_FACTORY.close();
		}
		
		if (E_FACTORY != null && E_FACTORY.isOpen()) {
			E_FACTORY.close();
		}
		
		if (R_FACTORY != null && R_FACTORY.isOpen()) {
			R_FACTORY.close();
		}
	}
	
}
