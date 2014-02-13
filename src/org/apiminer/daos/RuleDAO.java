package org.apiminer.daos;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apiminer.SystemProperties;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.mining.Rule;
import org.apiminer.util.DatabaseUtil;

public class RuleDAO extends GenericDAO {

	public List<Rule> findByResult(long miningResultId) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try {
			 return em.createQuery("SELECT r FROM Rule r WHERE r.miningResult.id = :miningResultId ", Rule.class)
					 .setParameter("miningResultId", miningResultId)
					 .getResultList();
		} finally {
			em.close();
		}
	}

	public List<Rule> findByResultAndPremise(long miningResultId, Collection<ApiMethod> withApiMethodsAsPremises) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try {
			StringBuilder sb = new StringBuilder()
				.append("SELECT r ")
				.append("FROM Rule r JOIN r.premise as p ")
				.append("WHERE r.miningResult.id = :miningResultId ")
				.append("AND size(p.elements) = :size ");
			
			for (int i = 0; i < withApiMethodsAsPremises.size(); i++) {
				sb.append(String.format("AND :m%d MEMBER OF p.elements ", i));
			}
			
			sb.append("ORDER BY r.totalSupport DESC ");
			
			TypedQuery<Rule> query = em.createQuery(sb.toString(), Rule.class)
					 		.setParameter("miningResultId", miningResultId)
					 		.setParameter("size", withApiMethodsAsPremises.size());
			
			ApiMethod[] arrayCollection = withApiMethodsAsPremises.toArray(new ApiMethod[0]);
			for (int i = 0; i < withApiMethodsAsPremises.size(); i++) {
				query.setParameter(String.format("m%d", i), arrayCollection[i]);
			}
			
			return query.getResultList();
			
		} finally {
			em.close();
		}
	}

	public List<Rule> findAll() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try {
			 return em.createQuery("SELECT r FROM Rule r ", Rule.class).getResultList();
		} finally {
			em.close();
		}
	}

	@Override
	public Class<?> getObjectType() {
		return Rule.class;
	}

}
