package org.apiminer.daos;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.entities.example.RecommendedSet;
import org.apiminer.util.DatabaseUtil;

public class RecommendedSetDAO extends GenericDAO<RecommendedSet> {

	public List<RecommendedSet> findAllRecommendedSets() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.createQuery("SELECT rs FROM RecommendedSet rs ", RecommendedSet.class)
						.getResultList();
		}finally{
			em.close();
		}
	}
	
	public List<RecommendedSet> findAllRecommendedSets(int startPage, int maxPerPage) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.createQuery("SELECT rs FROM RecommendedSet rs ORDER BY rs.id", RecommendedSet.class)
						.setMaxResults(maxPerPage)
						.setFirstResult(startPage*maxPerPage)
						.getResultList();
		}finally{
			em.close();
		}
	}

	public void update(RecommendedSet object) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			em.getTransaction().begin();
			RecommendedSet object2 = em.find(RecommendedSet.class, object.getId());
			object2.setMethodSet(object.getMethodSet());
			object2.setRecommendedCombination(object.getRecommendedCombination());
			object2.setRecommendedExamples(object.getRecommendedExamples());
			object2.setRecommendedSetFactor(object.getRecommendedSetFactor());
			object2.setRuleBased(object.getRuleBased());
			em.merge(object2);
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}
	
	

}
