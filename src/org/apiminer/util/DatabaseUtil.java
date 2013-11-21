package org.apiminer.util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.apiminer.daos.Database;
import org.apiminer.daos.DatabaseType;

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
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("javax.persistence.jdbc.driver", DATABASE.getDriverClass());
		properties.put("javax.persistence.jdbc.url", DATABASE.getPreProcessingUrl());
		properties.put("javax.persistence.jdbc.user", DATABASE.getUsername());
		properties.put("javax.persistence.jdbc.password", DATABASE.getPassword());
		
		properties.put("eclipselink.connection-pool.default.initial", "1");
		properties.put("eclipselink.connection-pool.default.min", "64");
		properties.put("eclipselink.connection-pool.default.max", "64");
		
		properties.put("eclipselink.connection-pool.node2.driver", DATABASE.getDriverClass());
		properties.put("eclipselink.connection-pool.node2.url", DATABASE.getExamplesUrl());
		properties.put("eclipselink.connection-pool.node2.user", DATABASE.getUsername());
		properties.put("eclipselink.connection-pool.node2.password", DATABASE.getPassword());
		properties.put("eclipselink.connection-pool.node2.initial", "1");
		properties.put("eclipselink.connection-pool.node2.min", "64");
		properties.put("eclipselink.connection-pool.node2.max", "64");

		properties.put("eclipselink.partitioning", "Replicate");

		properties.put("eclipselink.ddl-generation.output-mode", "database");
		properties.put("eclipselink.target-database", DATABASE.getName());
		properties.put("eclipselink.logging.level", "SEVERE");
		
		properties.put("eclipselink.jdbc.batch-writing", "JDBC");
		properties.put("eclipselink.cache.size.default", "1000");
		properties.put("eclipselink.persistence-context.flush-mode", "commit");
		
		if (DATABASE.isAutoUpdate()) {
			properties.put("eclipselink.ddl-generation", "create-or-extend-tables");
		}
		
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
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("javax.persistence.jdbc.driver", database.getDriverClass());
		properties.put("javax.persistence.jdbc.user", database.getUsername());
		properties.put("javax.persistence.jdbc.password", database.getPassword());
		
		properties.put("eclipselink.connection-pool.default.initial", "1");
		properties.put("eclipselink.connection-pool.default.min", "64");
		properties.put("eclipselink.connection-pool.default.max", "64");
		
		properties.put("eclipselink.ddl-generation.output-mode", "database");
		properties.put("eclipselink.target-database", database.getName());
		properties.put("eclipselink.logging.level", "SEVERE");
		
		properties.put("eclipselink.jdbc.batch-writing", "JDBC");
		properties.put("eclipselink.cache.size.default", "1000");
		properties.put("eclipselink.persistence-context.flush-mode", "commit");
		
		if (database.isAutoUpdate()) {
			properties.put("eclipselink.ddl-generation", "create-or-extend-tables");
		}
		
		properties.put("javax.persistence.jdbc.url", database.getExamplesUrl());
		DatabaseUtil.E_FACTORY = Persistence.createEntityManagerFactory("examples", properties);
		properties.put("javax.persistence.jdbc.url", database.getPreProcessingUrl());
		DatabaseUtil.PP_FACTORY = Persistence.createEntityManagerFactory("pre-processing", properties);
		DatabaseUtil.DATABASE = database;
		
		buildReplicatedEntityManagerFactory();
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
