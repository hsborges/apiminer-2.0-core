package org.apiminer.checker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.ProjectDAO;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.example.Example;
import org.apiminer.tasks.AbstractTask;
import org.apiminer.tasks.TaskResult;
import org.apiminer.tasks.TaskStatus;
import org.apiminer.tasks.TasksController;
import org.apiminer.util.FilesUtil;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilationChecker {
	
	private static final Logger LOGGER = Logger.getLogger(CompilationChecker.class);
	
	private static final String DEFAULT_CODE_VERSION = JavaCore.VERSION_1_6;
	
	private static final String ABSTRACT_CODE = "%s public class ASTTester %s { public void _main_() %s }";

	private TasksController tasksController = TasksController.getInstance();
	
	public void checkAllExamples() {
		ProjectDAO projectDAO = null;
		try {
			projectDAO = new ProjectDAO();
			
			// add as import all API classes
			Project sourceProject = projectDAO.findSourceAPI();
			Set<ApiClass> apiClasses = sourceProject.getApiClass();
			Set<String> classesStr = new HashSet<String>();
			for (ApiClass apiClass : apiClasses) {
				classesStr.add(new String().concat("import ").concat(apiClass.getName().substring(0, apiClass.getName().indexOf(".", 8))).concat(".*;").concat("\n"));
			}
			StringBuilder defaultImports = new StringBuilder();
			for (String classe : classesStr) {
				defaultImports.append(classe);
			}
			
			List<Project> clients = projectDAO.findAllClients(DatabaseType.EXAMPLES);
			for (int i = 0; i < clients.size(); i++) {
				Project client = clients.get(i);
				LOGGER.info(String.format("Project %d of %d added on ThreadController.", i+1, clients.size()));
				tasksController.addTask(new CompilationCheckerTask(client, sourceProject, defaultImports.toString(), i));
			}

		} catch (Exception e) {
			LOGGER.error(e);
		}
		
//		while(tasksController.hasTasks()){
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {}
//		}
	}
	
	private static class CompilationCheckerTask extends AbstractTask {
		
		private Project api;
		
		private Project client;
		
		private String defaultImports;
		
		private int threadId;
		
		private ExampleDAO exampleDAO;
		
		public CompilationCheckerTask(Project client, Project source, String defaultImports, int threadId) {
			super();
			this.api = source;
			this.client = client;
			this.defaultImports = defaultImports;
			this.threadId = threadId;
			this.exampleDAO = new ExampleDAO();
		}

		@Override
		public void execute() {
			super.setStatus(TaskStatus.RUNNING);
			
			try {
				LOGGER.info(String.format("Analysing examples of project %s. (Thread %d)", client.getName(), threadId));
				for (Example example : this.exampleDAO.findByProject(client.getId())) {
					// configure classpath
					String[] classpath = client.getRepository().getJars().toArray(new String[0]);
					// configure sourcepath
					ArrayList<String> srcs = FilesUtil.collectDirectories(client.getRepository().getSourceFilesDirectory(),"src");
					srcs.add(api.getRepository().getSourceFilesDirectory());
					String[] sourcePath = srcs.toArray(new String[0]);
					// add imports
					String imports = "";
					for (String _import : example.getImports()) {
						imports.concat("import ").concat(_import).concat(";").concat("\n");
					}
					// add super class
					String superMethod = null;
					String extendsStr = "";
					for (String seed : example.getSeeds()) {
						if (seed.startsWith("super.") || !seed.contains(".")) {
							for (ApiElement apm : example.getApiMethods()) {
								if (seed.startsWith(apm.getName())) {
									superMethod = ((ApiMethod) apm).getApiClass().getName();
									extendsStr = "extends ".concat(superMethod);
									break;
								}
							}
						}
					}
					// format the example
					String code = String.format(ABSTRACT_CODE, defaultImports
							+ imports, extendsStr,
							example.getFormattedCodeExample());
					// compile
					CompilationUnit cu = CompilationChecker.getCompilationUnit(code, classpath, sourcePath);
					
					example.setHasProblems(false);
					example.setProblems(new LinkedList<Integer>());
					if (cu.getProblems().length > 0) {
						example.setHasProblems(true);
						for (IProblem problem : cu.getProblems()) {
							if (problem.isError()) {
								example.getProblems().add(problem.getID());
							}
						}
					}
					exampleDAO.update(example, DatabaseType.EXAMPLES);
				}
				
				super.setResult(TaskResult.SUCCESS);
			} catch (Exception e) {
				super.setResult(e);
			} finally {
				super.setStatus(TaskStatus.FINISHED);
			}
			
		}

		@Override
		public String toString() {
			return String.format("Task to analyze the examples of project %s. (Thread %d)", this.client.getName(), this.threadId);
		}
		
	}
	
	private static CompilationUnit getCompilationUnit(String code, String[] classPath, String[] sourcePath){
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setUnitName("ASTTester");
		parser.setResolveBindings(true);
//		parser.setBindingsRecovery(true);
//		parser.setStatementsRecovery(true);
		parser.setEnvironment(classPath, sourcePath, null, true);
		parser.setSource(code.toCharArray());
		
		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, DEFAULT_CODE_VERSION);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, DEFAULT_CODE_VERSION);
		options.put(JavaCore.COMPILER_SOURCE, DEFAULT_CODE_VERSION);

		parser.setCompilerOptions(options);
		
		return (CompilationUnit) parser.createAST(new NullProgressMonitor());
	}
	
}
