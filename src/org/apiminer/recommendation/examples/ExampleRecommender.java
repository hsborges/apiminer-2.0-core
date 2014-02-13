package org.apiminer.recommendation.examples;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.GenericDAO;
import org.apiminer.daos.AssociatedElementDAO;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.Recommendation;

public class ExampleRecommender {
	
	private final ExampleComparator exampleComparator = new ExampleComparator();
	
	private final ExampleDAO exampleDAO = new ExampleDAO();

	public void makeRecommendations(AssociatedElement associatedElement) {
		Set<ApiElement> elements = new HashSet<ApiElement>();
		elements.addAll(associatedElement.getElements());
		elements.add(associatedElement.getRecommendedAssociations().getFromElement());
		
		List<Example> examples = exampleDAO.findExamplesWithMethods(elements);
		Collections.sort(examples, exampleComparator);
		
		associatedElement.setRecommendedExamples(examples);
		new AssociatedElementDAO().update(associatedElement, DatabaseType.EXAMPLES);
	}
	
	public void makeRecommendations(ApiMethod apiMethod) {
		Set<ApiElement> elements = new HashSet<ApiElement>();
		elements.add(apiMethod);
		
		List<Example> examples = exampleDAO.findExamplesWithMethods(elements);
		Collections.sort(examples, exampleComparator);
		
		GenericDAO dao = new GenericDAO() {
			@Override
			public Class<?> getObjectType() {
				return Recommendation.class;
			}
		};
		
		Recommendation rec = (Recommendation) dao.find(apiMethod.getId(), DatabaseType.EXAMPLES);
		
		if (rec == null) {
			rec = new Recommendation();
			rec.setFromElement(apiMethod);
			rec.setAssociatedElements(null);
			dao.persist(rec, DatabaseType.EXAMPLES);
		}

		rec.setElementExamples(examples);
		rec.setAddedAt(new Date());
		
		dao.update(rec, DatabaseType.EXAMPLES);
	}
	

}
