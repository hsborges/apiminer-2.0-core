package org.apiminer.checker;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.entities.example.Example;

/**
 * @author Hudson S. Borges
 *
 */
public class SeedsChecker {
	
	private static final Logger LOGGER = Logger.getLogger(SeedsChecker.class);
	
	public static final void checkAllExamples(){
		
		ExampleDAO exampleDAO = new ExampleDAO();
		
		LOGGER.info("Obtaining all examples");
		LinkedList<Example> list = new LinkedList<Example>(exampleDAO.findAll());
		
		LOGGER.info("Analyzing the examples");
		while (!list.isEmpty()) {
			Example ex = list.removeFirst();
			for (String s : ex.getSeeds()) {
				if (!ex.getCodeExample().contains(s)) {
					LOGGER.info(String.format("Example %s invalid, setting as problem ... ", ex.getId()));
					ex.setHasProblems(true);
					exampleDAO.update(ex, DatabaseType.EXAMPLES);
				}
			}
			
		}
	}

}
