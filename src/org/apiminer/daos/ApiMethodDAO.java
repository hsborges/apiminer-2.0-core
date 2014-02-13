package org.apiminer.daos;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apiminer.SystemProperties;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.util.DatabaseUtil;

public class ApiMethodDAO extends GenericDAO {

	public List<ApiMethod> findUsedMethods(Integer supportMin) {

		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try {
			
			StringBuilder sb = new StringBuilder()
				.append("SELECT invocation ")
				.append("FROM Transaction t JOIN t.invocations as invocation ")
				.append("WHERE size(t.invocations) > 1 ")
				.append("GROUP BY invocation ");
			
			if (supportMin != null) {
				sb.append("HAVING COUNT(invocation) >= :supportMin ");
			}
			
			TypedQuery<ApiMethod> query = em.createQuery(sb.append("ORDER BY invocation.id ").toString(), ApiMethod.class);
			
			if (supportMin != null) {
				return query.setParameter("supportMin",supportMin).getResultList();
			}else{
				return query.getResultList();
			}
			
		} finally {
			em.close();
		}
		
	}

	public ApiMethod find(long projectId, String name, String className, List<String> params, DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			StringBuilder sb = new StringBuilder()
				.append("SELECT apm ")
				.append("FROM ApiMethod apm ")
				.append("WHERE apm.name like :name ")
				.append("AND apm.apiClass.name = :className ")
				.append("AND apm.parametersType = :params ")
				.append("AND apm.apiClass.project.id = :projectId ");
			
			Object result = null;
			try {
				result = em.createQuery(sb.toString())
						.setParameter("name", name)
						.setParameter("className", className)
						.setParameter("params", params)
						.setParameter("projectId", projectId)
						.setMaxResults(1)
						.getSingleResult();
			} catch (NoResultException e) {}
			
			if (result != null) {
				return (ApiMethod) result;
			}else{
				return null;
			}
			
		}finally{
			em.close();
		}
		
	}

	public List<ApiMethod> findByClass(long apiClassId, DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			return em.createQuery("SELECT apm FROM ApiMethod apm WHERE apm.apiClass.id = :apiClassId", ApiMethod.class)
					.setParameter("apiClassId", apiClassId)
					.getResultList();
		}finally{
			em.close();
		}
	}

	public ApiMethod find(long projectId, String fullName, DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			
			Object result = null;
			try {
				result = em.createQuery("SELECT apm FROM ApiMethod apm WHERE (apm.fullName = :fullName OR apm.simpleFullName = :fullName) AND apm.apiClass.project.id = :projectId")
						.setParameter("fullName", fullName)
						.setParameter("projectId", projectId)
						.setMaxResults(1)
						.getSingleResult();
			} catch (NoResultException e) {}
			
			if (result != null) {
				return (ApiMethod) result;
			}else{
				return null;
			}
		}finally{
			em.close();
		}
	}

	@Override
	public Class<?> getObjectType() {
		return ApiMethod.class;
	}

}
