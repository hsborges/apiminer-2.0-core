package org.apiminer.daos;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.entities.mining.MiningResult;
import org.apiminer.entities.mining.Rule;
import org.apiminer.util.DatabaseUtil;

public class MiningDAO extends GenericDAO<MiningResult> {
	
	private EntityManager em;

	public List<MiningResult> findAll() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try{
			return em.createQuery("SELECT mr FROM MiningResult mr ORDER BY mr.addedAt ASC ", MiningResult.class)
					.getResultList();
		}finally{
			em.close();
		}
	}

	public void beginTransaction() {
		if (em != null) {
			if (em.getTransaction().isActive()) {
				throw new PersistenceException("Close the current transaction before");
			}else if (em.isOpen()) {
				em.close();
			}
		}
		em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		em.getTransaction().begin();
		
	}

	public void commitAndCloseTransaction() {
		if (em == null || !em.getTransaction().isActive()) {
			throw new PersistenceException("Don't have transactions active");
		}else if (em.getTransaction().isActive()) {
			em.getTransaction().commit();
			em.close();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (em != null) {
			if (em.isOpen()) {
				if(em.getTransaction().isActive()){
					em.getTransaction().rollback();
				}
				em.close();
			}
		}
		super.finalize();
	}

	public void incrementalPersist(MiningResult result) {
		if (em != null && em.getTransaction().isActive()) {
			em.persist(result);
		}else{
			throw new PersistenceException("No transactions is alive!");
		}
	}

	public void incrementalPersist(Rule rule) {
		if (em != null && em.getTransaction().isActive()) {
			em.persist(rule);
		}else{
			throw new PersistenceException("No transactions is alive!");
		}
	}

	public void incrementalFlush() {
		if (em != null && em.getTransaction().isActive()) {
			em.flush();
		}else{
			throw new PersistenceException("No transactions is alive!");
		}
	}

}
