package org.apiminer.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.example.Example;
import org.apiminer.util.FilesUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

/**
 * 	Classe responsável por extrair exemplos de uso de métodos de sistemas clientes.
 * 
 * @author Hudson S. Borges
 *
 */
public class ExampleExtractor {
	
	private static final Logger LOGGER = Logger.getLogger(ExampleExtractor.class);
	
	public static final String DEFAULT_CODE_VERSION = JavaCore.VERSION_1_7;
	
	private ASTParser parser;
	
	private String sourceCodeFolder;
	
	private List<String> sourceCodeFiles = new LinkedList<String>();
	private List<String> jarsDependencies = new LinkedList<String>();
	
	private final long sourceProjectId;
	
	private List<Example> examples;
	private Set<ApiClass> apiClasses;
	
	/**
	 * @param sourceCodeFolder
	 * @param jars
	 * @param sourceProjectId
	 */
	public ExampleExtractor(String sourceCodeFolder, Collection<String> jars, long sourceProjectId) {
		this.parser = ASTParser.newParser(AST.JLS3);
		this.sourceCodeFolder = sourceCodeFolder;
		this.sourceCodeFiles.addAll(FilesUtil.collectFiles(sourceCodeFolder, ".java"));
		this.jarsDependencies.addAll(jars);
		this.jarsDependencies.addAll(FilesUtil.collectFiles(sourceCodeFolder, ".jar"));
		this.sourceProjectId = sourceProjectId;
	}
	
	private void setOptions() {

		this.parser.setEnvironment(
				this.jarsDependencies.toArray(new String[0]),  // Jars
				new String[]{sourceCodeFolder},  // Source code
				null, true);
		
		this.parser.setResolveBindings(true);
		this.parser.setBindingsRecovery(true);
		this.parser.setKind(ASTParser.K_COMPILATION_UNIT);

		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, DEFAULT_CODE_VERSION);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, DEFAULT_CODE_VERSION);
		options.put(JavaCore.COMPILER_SOURCE, DEFAULT_CODE_VERSION);

		this.parser.setCompilerOptions(options);
	}
	
	/**
	 * Realiza o parser e a extração de exemplos dos arquivos java existentes na pasta indicada no construtor da classe.
	 */
	public void parse(){
		
		LOGGER.debug("Setting up the properties of the parser");
		this.setOptions();
		
		LOGGER.debug("Building the ASTParser");
		final ExampleExtractorVisitor visitor = new ExampleExtractorVisitor(this.sourceProjectId);
		 
		FileASTRequestor fileASTRequestor = new FileASTRequestor() { 
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				LOGGER.debug("Extracting from source file: " + sourceFilePath);
				ast.accept(visitor); 
			} 
		};
		
		LOGGER.debug("Extacting examples from source files"); 
		parser.createASTs(sourceCodeFiles.toArray(new String[0]), 
				null, 
				new String[0], 
				fileASTRequestor, 
				null); 
		
		LOGGER.debug(String.format("Extraction of examples process %s finished", sourceCodeFolder));
		
		this.examples = new ArrayList<Example>(visitor.getExamples());
		this.apiClasses = new HashSet<ApiClass>(visitor.getApiClasses());
		
	}
	
	/**
	 * @return
	 */
	public String getSourceCodeFolder() {
		return sourceCodeFolder;
	}

	/**
	 * @return
	 */
	public List<String> getSourceCodeFiles() {
		return sourceCodeFiles;
	}

	/**
	 * @return
	 */
	public Collection<String> getJarsDependencies() {
		return jarsDependencies;
	}

	/**
	 * @return
	 */
	public Collection<Example> getExamples() {
		return examples;
	}
	
	/**
	 * @return
	 */
	public Set<ApiClass> getApiClasses() {
		return apiClasses;
	}

}
