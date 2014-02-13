package org.apiminer.analyzer.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apiminer.entities.ProjectAnalyserStatistic;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.util.FilesUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;

/**
 * @author Hudson S. Borges
 *
 */
public class APIAnalyzer {
	
	private static final Logger LOGGER = Logger.getLogger(APIAnalyzer.class);

	/**
	 * default compiling version
	 */
	public static final String DEFAULT_CODE_VERSION = JavaCore.VERSION_1_6;

	/**
	 * list of files that will be analyzed
	 */
	private Collection<String> files;

	private String sourceCodeDirectory;
	
	private Collection<String> jars;
	
	private List<String> sourceDirectories;

	private boolean includePublic;
	private boolean includePrivate;
	private boolean includeProtected;

	private Set<ApiClass> apiClasses;
	
	private ProjectAnalyserStatistic statistics;

	/**
	 * Java parser, provided by JDT
	 */
	private ASTParser parser;

	private APIAnalyzer(String sourceCodeDirectory) {
		if (sourceCodeDirectory == null) {
			throw new IllegalArgumentException("Source code path must be not null");
		}

		File sourcePathFile = new File(sourceCodeDirectory);

		if (!sourcePathFile.exists() || !sourcePathFile.isDirectory()) {
			throw new IllegalArgumentException("Source code path must be a directory");
		}

		this.parser = ASTParser.newParser(AST.JLS3);
		this.files = FilesUtil.collectFiles(sourceCodeDirectory, ".java", true);
		this.jars = FilesUtil.collectFiles(sourceCodeDirectory, ".jar", true);
		this.sourceDirectories = FilesUtil.collectDirectories(sourceCodeDirectory, "src");
		this.sourceDirectories.add(sourceCodeDirectory);
	}

	public APIAnalyzer(String sourceFilesDirectory,
			String[] jarsDependencies, boolean includePublic,
			boolean includePrivate, boolean includeProtected) {

		this(sourceFilesDirectory);

		this.includePublic = includePublic;
		this.includePrivate = includePrivate;
		this.includeProtected = includeProtected;

		this.sourceCodeDirectory = sourceFilesDirectory;
		
		if (jarsDependencies != null && jarsDependencies.length > 0) {
			this.jars.addAll(Arrays.asList(jarsDependencies));
		}
	}

	/**
	 * set the parser options
	 */
	private void setOptions() {
		parser.setEnvironment(
				jars.toArray(new String[0]),
				sourceDirectories.toArray(new String[0]),
				null,
				true);

		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, DEFAULT_CODE_VERSION);
		options.put(
				JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				DEFAULT_CODE_VERSION);
		options.put(JavaCore.COMPILER_SOURCE, DEFAULT_CODE_VERSION);

		parser.setCompilerOptions(options);
	}

	public Set<ApiClass> getApiClasses() {
		return apiClasses;
	}

	public void parse() {
		this.setOptions();

		final APIVisitor visitor = new APIVisitor(
				this.includePublic,
				this.includePrivate,
				this.includeProtected);

		LOGGER.debug("Analysing the source files on directory "+sourceCodeDirectory);
		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit ast) {
				LOGGER.debug("Analysing source file: " + sourceFilePath);
				ast.getRoot().accept(visitor);
			}
		};

		parser.createASTs(
				(files.toArray(new String[files.size()])),
				null,
				new String[0],
				requestor,
				null);
		
		System.gc();

		LOGGER.debug("Analyse of source code files in the directory "
				+ sourceCodeDirectory + " finished");
		
		this.apiClasses = visitor.getApiClasses();
		this.statistics = visitor.getStatistics();
	}

	/**
	 * @return the statistics
	 */
	public ProjectAnalyserStatistic getStatistics() {
		return statistics;
	}

	public Collection<String> getJarsDependencies() {
		return jars;
	}

}
