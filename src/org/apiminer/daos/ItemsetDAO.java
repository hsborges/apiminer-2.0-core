package org.apiminer.daos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;

import org.apiminer.SystemProperties;
import org.apiminer.entities.mining.Itemset;
import org.apiminer.util.DatabaseUtil;

public class ItemsetDAO extends GenericDAO<Itemset> {

	public List<Itemset> findDistinctPremiseElements(long miningResultId, int itemsetMaxSize) {
		
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try{
			StringBuilder sb = new StringBuilder()
				.append("SELECT premise ")
				.append("FROM Rule r JOIN r.premise as premise ")
				.append("WHERE r.miningResult.id = :miningResultId ")
				.append("AND size(premise.elements) <= :itemsetMaxSize ");
			
			List<Itemset> tempList = em.createQuery(sb.toString(), Itemset.class)
					.setParameter("miningResultId", miningResultId)
					.setParameter("itemsetMaxSize", itemsetMaxSize)
					.getResultList();
			
			return new ArrayList<Itemset>(new HashSet<Itemset>(tempList));
			
		}finally{
			em.close();
		}
		
	}
	
	public List<Itemset> findDistinctConsequenceElements(long miningResultId, int itemsetMaxSize) {
		
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try{
			StringBuilder sb = new StringBuilder()
				.append("SELECT consequence ")
				.append("FROM Rule r JOIN r.consequence as consequence ")
				.append("WHERE r.miningResult.id = :miningResultId ")
				.append("AND size(consequence.elements) <= :itemsetMaxSize ");
			
			List<Itemset> tempList = em.createQuery(sb.toString(), Itemset.class)
					.setParameter("miningResultId", miningResultId)
					.setParameter("itemsetMaxSize", itemsetMaxSize)
					.getResultList();
			
			return new ArrayList<Itemset>(new HashSet<Itemset>(tempList));
			
		}finally{
			em.close();
		}
		
	}

}
