package org.apiminer.recommendation.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.example.Example;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.tasks.TasksController;
import org.apiminer.util.LoggerUtil;
import org.eclipse.jdt.core.compiler.IProblem;

public class UniqueExampleSorter {
	
	private ProjectDAO projectDAO;
	private TasksController threadController = TasksController.getInstance();
	private TreeSet<Integer> acceptProblems;
	
	{
		LoggerUtil.logEvents();
	}
	
	public UniqueExampleSorter() {
		super();
		this.acceptProblems = new TreeSet<Integer>();
		this.acceptProblems.add(IProblem.UnusedImport);
		this.acceptProblems.add(IProblem.AbstractMethodMustBeImplemented);
		this.acceptProblems.add(IProblem.UndefinedConstructorInDefaultConstructor);
		this.acceptProblems.add(IProblem.NotVisibleConstructorInDefaultConstructor);
		this.projectDAO = new ProjectDAO();
	}

	public void sortAll() {
		Project sourceProject = projectDAO.findSourceAPI(DatabaseType.EXAMPLES);
		for (ApiClass apc : sourceProject.getApiClass()) {
			for (ApiMethod apm : apc.getApiMethods()) {
				this.threadController.addTask(new InternalTask(apm));
			}
		}
		while(this.threadController.hasTasks()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
	}
	
	private class InternalTask extends AbstractTask {

		private ExampleDAO exampleDAO;
		private ApiMethod apiMethod;
		
		public InternalTask(ApiMethod apiMethod) {
			super();
			this.apiMethod = apiMethod;
			this.exampleDAO = new ExampleDAO();
			super.status = TaskStatus.WAITING;
		}

		@Override
		public void execute() {
			super.status = TaskStatus.RUNNING;
			try{
				List<Example> examples = exampleDAO.findByMethod(apiMethod.getId());
				if (!examples.isEmpty()) { 
					Collections.sort(examples, exampleComparator);
					Map<Long, Integer> aux = new HashMap<Long, Integer>();
					for (int i = 0; i < examples.size(); i++) {
						aux.put(examples.get(i).getId(), i);
					}
					this.exampleDAO.updateSingleExamplesPositions(apiMethod, aux);
				}
				super.result = TaskResult.SUCCESS;
			}catch(Exception e){
				super.result = TaskResult.FAILURE;
				super.result.setProblem(e);
			}
			super.status = TaskStatus.FINISHED;
		}

		@Override
		public String toString() {
			return "Compiling usage examples of method "+apiMethod.getFullName();
		}
		
	}
	
	private final Comparator<Example> exampleComparator = new Comparator<Example>() {
		@Override
		public int compare(Example ex1, Example ex2) {
			ArrayList<Integer> p1 = new ArrayList<Integer>(ex1.getProblems());
			ArrayList<Integer> p2 = new ArrayList<Integer>(ex2.getProblems());
			
			if (p1.isEmpty() || p2.isEmpty()) {
				if (p1.isEmpty() && !p2.isEmpty()) {
					return 1;
				}else if (!p1.isEmpty() && p2.isEmpty()) {
					return -1;
				}else {
					return ex1.getFormattedCodeExample().length() - ex2.getFormattedCodeExample().length();
				}
			}else{
				p1.removeAll(acceptProblems);
				p2.removeAll(acceptProblems);
				
				if (p1.size() > p2.size()) {
					return 1;
				}else if (p1.size() < p2.size()){
					return -1;
				} else {
					return ex1.getFormattedCodeExample().length() - ex2.getFormattedCodeExample().length();
				}
			}
		}
	};
	
}
