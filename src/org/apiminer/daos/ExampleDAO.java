package org.apiminer.daos;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.apiminer.SystemProperties;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.example.Example;
import org.apiminer.util.DatabaseUtil;

public class ExampleDAO extends GenericDAO<Example> {
	
	public void persist(Project project, Collection<Example> examples) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		this.prePersist(project, examples);
		try{
			em.getTransaction().begin();
			em.persist(project);
			for (Example ex : examples) {
				if (ex.getFormattedCodeExample() != null && ex.getSourceMethodCall() != null
						&& ex.getCodeExample() != null) {
					em.persist(ex);
				}	
			}
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}
	
	private void prePersist(Project project, Collection<Example> examples){
		for (ApiClass apiClass : project.getApiClass()) {
			if (apiClass.getProject() == null) {
				apiClass.setProject(project);
			}
			for (ApiMethod apiMethod : apiClass.getApiMethods()) {
				if (apiMethod.getApiClass() == null) {
					apiMethod.setApiClass(apiClass);
				}
			}
		}
		for (Example ex : examples) {
			ex.setProject(project);
		}
	}
	
	public List<Example> findExamplesWithMethods(Collection<ApiMethod> methods) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			StringBuilder sb = new StringBuilder()
									.append("SELECT e ")
									.append("FROM Example e ")
									.append("WHERE SIZE(e.apiMethods) = :numMethods ");
			
			for (int i = 0; i < methods.size(); i++) {
				sb.append(String.format("AND :apm%d MEMBER OF e.apiMethods ", i));
			}
			sb.append("ORDER BY e.id ");
			
			ApiMethod[] arrayMethods = methods.toArray(new ApiMethod[0]);
			TypedQuery<Example> query = em.createQuery(sb.toString(), Example.class)
									  .setParameter("numMethods", methods.size());
				
			for (int i = 0; i < methods.size(); i++) {
				query.setParameter(String.format("apm%d", i), arrayMethods[i]);
			}
			
//			LinkedList<Example> resultList = new LinkedList<Example>(query.getResultList());
//			List<Example> result = new LinkedList<Example>();
//			
//			while(!resultList.isEmpty()){
//				Example ex = resultList.removeFirst();
//				if (ex.getApiMethods().containsAll(methods)) {
//					result.add(ex);
//				}
//			}
//					
//			return result;
			
			return new LinkedList<Example>(query.getResultList());
			
		}finally{
			em.close();
		}
	}

	public void changeExamples(long projectId, Collection<Example> newExamples) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			em.getTransaction().begin();
			Project project = em.find(Project.class, projectId);
			
			//Find old examples to remove before
			List<Example> examplesOld = em.createQuery("SELECT e FROM Example e WHERE e.project.id = :projectId", Example.class)
											.setParameter("projectId", projectId)
											.getResultList();
			
			// Remove old examples
			for (Example example : examplesOld) {
				em.remove(example);
			}
			
			// Persist new examples
			for (Example ex : newExamples) {
				ex.setProject(project);
				em.persist(ex);
			}
			
			//Commit the transaction
			em.getTransaction().commit();
		}catch(PersistenceException e){
			em.getTransaction().rollback();
			throw e;
		}finally{
			em.close();
		}
	}

	public List<Example> findAll() {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try{
			return em.createQuery("SELECT ex FROM Example ex ORDER BY ex.id", Example.class).getResultList();
		}finally{
			em.close();
		}
	}

	public List<Example> findByProject(long projectId) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try {
			return em.createQuery("SELECT ex FROM Example ex WHERE ex.project.id = :projectId ORDER BY ex.id", Example.class)
					.setParameter("projectId", projectId)
					.getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Example> findByMethod(long apiMethodId) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try {
			String query = "SELECT * FROM example ex JOIN example_apimethod exa ON exa.example_id = ex.id AND exa.method_id = %d JOIN (SELECT example_id FROM example_apimethod GROUP BY example_id HAVING COUNT (example_id) = 1) as R ON ex.id = R.example_id";
			return em.createNativeQuery(String.format(query, apiMethodId), Example.class).getResultList();
		} finally {
			em.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Example> findByMethod(String apiMethod) {
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try {
			String query = "SELECT * FROM example ex JOIN example_positions exp ON example_id = ex.id AND simple_full_name = '%s' ORDER BY row";
			return em.createNativeQuery(String.format(query, apiMethod), Example.class).getResultList();
		} finally {
			em.close();
		}
	}
	
	public void updateSingleExamplesPositions(ApiMethod apm, Map<Long, Integer> mapExamplesPositions){
		EntityManager em = DatabaseUtil.getEntityManager(SystemProperties.DATABASE, DatabaseType.EXAMPLES);
		try {
			em.getTransaction().begin();
			em.createNativeQuery(String.format("DELETE FROM example_positions WHERE method_id = %d", apm.getId()))
				.executeUpdate();
			for (Entry<Long, Integer> entry : mapExamplesPositions.entrySet()) {
				em.createNativeQuery(String.format("INSERT INTO example_positions(\"row\", method_id, simple_full_name, full_name, example_id) VALUES (%d, %d, '%s', '%s', %d)", entry.getValue(), apm.getId(), apm.getSimpleFullName(), apm.getFullName(), entry.getKey()))
					.executeUpdate();
			}
			em.getTransaction().commit();
		} catch (PersistenceException ex) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			throw ex;
		} finally {
			em.close();
		}
	}

}
