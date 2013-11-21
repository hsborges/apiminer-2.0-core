package org.apiminer.daos;

import javax.persistence.EntityManager;

import org.apiminer.SystemProperties;
import org.apiminer.entities.mining.Transaction;
import org.apiminer.util.DatabaseUtil;

public class TransactionDAO extends GenericDAO<Transaction> {

	public long numTotalTransactions(boolean onlyValid) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try{
			StringBuilder sb = new StringBuilder()
				.append("SELECT COUNT(t) ")
				.append("FROM Transaction t ")
				.append("WHERE t.sourceApiMethod.apiClass.project.clientOf IS NULL ");
			
			if (onlyValid) {
				sb.append("AND size(t.invocations) > 1 ");
			}
			
			return em.createQuery(sb.toString(), Long.class).getSingleResult();
		}finally{
			em.close();
		}
	}

}
