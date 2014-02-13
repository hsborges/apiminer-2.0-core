package org.apiminer.daos;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apiminer.SystemProperties;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.mining.Transaction;
import org.apiminer.util.DatabaseUtil;

public class ProjectDAO extends GenericDAO {

	public Project find(String projectName, DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			Object result = null;
			
			try {
				result = em.createQuery("SELECT pj FROM Project pj WHERE pj.name like :projectName")
						.setParameter("projectName", projectName)
						.setMaxResults(1)
						.getSingleResult();
			} catch (NoResultException e) {}
			
			if (result != null) {
				return (Project) result;
			}else{
				return null;
			}
		}finally{
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Project> findAllProjects(DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			return em.createQuery("SELECT p FROM Project p ORDER BY p.id ASC").getResultList();
		}finally{
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Project> findAllClients(DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try{
			return em.createQuery("SELECT pj FROM Project pj WHERE pj.clientOf IS NOT NULL").getResultList();
		}finally{
			em.close();
		}
	}

	public Project findSourceAPI() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.REPLICATED);
		try{
			try {
				return em.createQuery("SELECT pj FROM Project pj WHERE pj.clientOf IS NULL", Project.class)
						.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		}finally{
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> findTransactions(int minSize) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.PRE_PROCESSING);
		try {
			StringBuilder sb = new StringBuilder()
				.append("SELECT t ")
				.append("FROM Transaction t ")
				.append("WHERE t.sourceApiMethod.apiClass.project.clientOf.id IS NOT NULL ")
				.append("AND size(t.invocations) >= :minSize ");
			
			return em.createQuery(sb.toString())
						.setParameter("minSize", minSize)
						.getResultList();
			
		} finally {
			em.close();
		}
	}
	
	public void persistSourceAPI(Project project) {
		if (project.isSource()) {
			this.prePersist(project);
			EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.REPLICATED);
			try{
				em.getTransaction().begin();
				em.persist(project);
				em.getTransaction().commit();
			}catch(PersistenceException pe){
				em.getTransaction().rollback();
				throw pe;
			}finally{
				em.close();
			}
		}else{
			throw new PersistenceException("The project must be a source API");
		}
	}
	
	public void persist(Project object, DatabaseType databaseType) {
		this.prePersist(object);
		super.persist(object, databaseType);
	}

	private void prePersist(final Project project) {
		for (ApiClass apc : project.getApiClass()) {
			boolean hasConstructor = false;
			if (apc.getProject() == null)
				apc.setProject(project);
			for (ApiMethod apm : apc.getApiMethods()) {
				if (apm.isConstructor())
					hasConstructor = true;
				if (apm.getApiClass() == null)
					apm.setApiClass(apc);
			}
			if (!hasConstructor) {
				String[] splited = apc.getName().split("\\.");
				ApiMethod constructor = new ApiMethod(apc, splited[splited.length-1] , new ArrayList<String>());
				constructor.setConstructor(true);
				constructor.setHasJavadoc(true);
				constructor.setHidden(false);
				constructor.setPublic(true);
				constructor.setReturnType(apc.getName());
				
				apc.getApiMethods().add(constructor);
			}
		}
	}

	@Override
	public void delete(Object objectId, final DatabaseType databaseType) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, databaseType);
		try {
			em.getTransaction().begin();
			if (databaseType.equals(DatabaseType.EXAMPLES)) {
				List<Example> examples = em.createQuery("SELECT e FROM Example e WHERE e.project.id = :projectId", Example.class)
											.setParameter("projectId", objectId)
											.getResultList(); 
				for (Example example : examples) {
					em.remove(example);
				}
			}
			em.remove(em.find(Project.class, objectId));
			em.getTransaction().commit();
		} catch (PersistenceException e) {
			em.getTransaction().rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	public void removeClassesNotDocumented() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.REPLICATED);
		try{
			List<ApiClass> classes = em.createQuery("SELECT apc FROM ApiClass apc JOIN apc.project p WHERE apc.hasJavadoc IS FALSE AND p.clientOf IS NULL", ApiClass.class).getResultList();
			em.getTransaction().begin();
			for (ApiClass apc : classes) {
				em.remove(apc);
			}
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
		}finally{
			em.close();
		}
	}

	public void removeMethodsNotDocumented() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.REPLICATED);
		try{
			List<ApiMethod> methods = em.createQuery("SELECT apm FROM ApiMethod apm JOIN apm.apiClass apc JOIN apc.project p WHERE apm.hasJavadoc IS FALSE AND p.clientOf IS NULL", ApiMethod.class).getResultList();
			em.getTransaction().begin();
			for (ApiMethod apm : methods) {
				em.remove(apm);
			}
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
		}finally{
			em.close();
		}
	}

	public void removeTestClassesWithPostfix(String endsWith) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.REPLICATED);
		try{
			List<ApiClass> classes = em.createQuery("SELECT apc FROM ApiClass apc JOIN apc.project p WHERE apc.hasJavadoc IS FALSE AND apc.name LIKE '%:postfix' AND p.clientOf IS NULL", ApiClass.class)
					.setParameter("postfix", endsWith)
					.getResultList();
			em.getTransaction().begin();
			for (ApiClass apc : classes) {
				em.remove(apc);
			}
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
		}finally{
			em.close();
		}
	}

	@Override
	public Class<?> getObjectType() {
		return Project.class;
	}

}
