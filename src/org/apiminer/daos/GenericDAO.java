package org.apiminer.daos;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.util.DatabaseUtil;

public abstract class GenericDAO {
	
	public abstract Class<?> getObjectType();

	public void persist(Object object, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			em.getTransaction().begin();
			em.persist(object);
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public void update(Object object, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			em.getTransaction().begin();
			em.merge(object);
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public void delete(Object objectId, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			em.getTransaction().begin();
			em.remove(em.find(getObjectType(), objectId));
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public Object find(Object objectId, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			return em.find(getObjectType(), objectId);
		} catch (PersistenceException e) {
			throw e;
		} finally {
			em.close();
		}
	}
	
	


}
