package org.apiminer.daos;

import java.util.List;

import javax.persistence.EntityManager;

import org.apiminer.SystemProperties;
import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.util.DatabaseUtil;

public class AssociatedElementDAO extends GenericDAO {

	public List<AssociatedElement> findAllAssociatedElements() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.createQuery("SELECT ae FROM AssociatedElement ae ", AssociatedElement.class)
						.getResultList();
		}finally{
			em.close();
		}
	}
	
	public List<AssociatedElement> findAllAssociatedElements(int startPage, int maxPerPage) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.createQuery("SELECT ae FROM AssociatedElement ae ORDER BY ae.id", AssociatedElement.class)
						.setMaxResults(maxPerPage)
						.setFirstResult(startPage*maxPerPage)
						.getResultList();
		}finally{
			em.close();
		}
	}

	@Override
	public Class<?> getObjectType() {
		return AssociatedElement.class;
	}

}
