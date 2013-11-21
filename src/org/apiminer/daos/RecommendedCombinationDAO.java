package org.apiminer.daos;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.entities.example.RecommendedAssociation;
import org.apiminer.util.DatabaseUtil;

public class RecommendedCombinationDAO {

	public RecommendedAssociation find(String id) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.find(RecommendedAssociation.class, id);
		}finally{
			em.close();
		}
	}

	public void persist(RecommendedAssociation object) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			String key = RecommendedAssociation.generateKey(object);
			RecommendedAssociation rc = em.find(RecommendedAssociation.class, key);
			
			em.getTransaction().begin();
			if (rc != null) {
				em.remove(rc);
			}
			
			object.setId(key);
			em.persist(object);
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}
	
	public void delete(String id) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			RecommendedAssociation recommendedAssociation = em.find(RecommendedAssociation.class, id);
			if (recommendedAssociation == null) {
				throw new PersistenceException("Object not found!");
			}else{
				try {
					em.getTransaction().begin();
					em.remove(recommendedAssociation);
					em.getTransaction().commit();
				} catch (PersistenceException e) {
					em.getTransaction().rollback();
					throw e;
				}
			}
		}finally{
			em.close();
		}
	}

	public void persistOnBatch(List<RecommendedAssociation> recommendedAssociations) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			em.getTransaction().begin();
			for (RecommendedAssociation object : recommendedAssociations) {
				String key = RecommendedAssociation.generateKey(object);
				RecommendedAssociation rc = em.find(RecommendedAssociation.class, key);
				if (rc != null) {
					em.remove(rc);
				}
				
				object.setId(key);
				em.persist(object);
			}
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

}
