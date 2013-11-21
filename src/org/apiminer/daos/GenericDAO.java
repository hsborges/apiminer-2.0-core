package org.apiminer.daos;

import java.lang.reflect.ParameterizedType;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.daos.interfaces.IEntity;
import org.apiminer.daos.interfaces.IGenericDAO;
import org.apiminer.util.DatabaseUtil;

class GenericDAO <T extends IEntity> implements IGenericDAO<T> {
	
	@SuppressWarnings("unchecked")
	private final Class<T> typeClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

	@Override
	public void persist(T object, final DatabaseType databaseType) {
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

	@Override
	public void update(T object, final DatabaseType databaseType) {
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

	@Override
	public void delete(long object, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			em.getTransaction().begin();
			em.remove(em.find(typeClass, object));
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public T find(long id, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			return (T) em.find(typeClass, id);
		} catch (PersistenceException e) {
			throw e;
		} finally {
			em.close();
		}
	}
	
	


}
