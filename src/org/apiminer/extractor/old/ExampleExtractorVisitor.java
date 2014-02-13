package org.apiminer.extractor.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.entities.Attachment;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiEnum;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.ExampleMetric;
import org.apiminer.util.ASTUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class ExampleExtractorVisitor extends ASTVisitor {
	
	private static final Logger LOGGER = Logger.getLogger(ExampleExtractorVisitor.class);

	private Set<MethodInvocation> invocations = null;
	private Map<MethodInvocation, ApiMethod> mapInvocations = null;
	
	private final long sourceProjectId;
	
	private final Collection<Example> examples = new ArrayList<Example>();
	
//	private final Set<Set<ApiMethod>> setOfApiMethods = new HashSet<Set<ApiMethod>>();
	
	private Map<ITypeBinding, ApiClass> classMap = new HashMap<ITypeBinding, ApiClass>();
	private Map<IMethodBinding, ApiMethod> methodMap = new HashMap<IMethodBinding, ApiMethod>();
	private Map<ASTNode, Attachment> attachmentMap = new HashMap<ASTNode, Attachment>();
	
	public ExampleExtractorVisitor(long sourceProjectId) {
		
		super(true);
		
		if (sourceProjectId < 0) {
			throw new IllegalArgumentException("Invalid source project identificator");
		}
		
		this.sourceProjectId = sourceProjectId;
				
//		Stack<Rule> stack = new Stack<Rule>();
//		stack.addAll(DAOFactory.createRuleDAO().findAllDistinctRules());
//		while (!stack.isEmpty()) {
//			Rule r = stack.pop();
//			Set<ApiMethod> apiMethods = new HashSet<ApiMethod>();
//			apiMethods.addAll(r.getPremise().getElements() );
//			apiMethods.addAll(r.getConsequence().getElements());
//			this.setOfApiMethods.add(apiMethods);
//		}
		
	}
	
	@Override
	public void endVisit(MethodDeclaration node) {
		
		if (this.invocations.isEmpty()) {
			return;
		}
		
//		for (MethodInvocation invocation : this.invocations) {
//			
//			Set<MethodInvocation> envolvedInvocations = new HashSet<MethodInvocation>();
//			envolvedInvocations.add(invocation);
//			
//			List<ApiMethod> envolvedApiMethods = new ArrayList<ApiMethod>();
//			envolvedApiMethods.add(this.mapInvocations.get(invocation));
//			
//			List<String> envolvedSeeds = new ArrayList<String>();
//			envolvedSeeds.add(invocation.toString());
//			
//			this.examples.add(makeExample(node, envolvedInvocations, envolvedApiMethods, envolvedSeeds));
//			
//		}
		
		int invocationsSize = this.invocations.size();
		MethodInvocation[] invocationsArray = this.invocations.toArray(new MethodInvocation[invocationsSize]);
		
		int i = 0;
		char[] defaultBinaryNameArray = new char[invocationsSize];
		Arrays.fill(defaultBinaryNameArray, '1');
		StringBuilder sb = null;
		do {
			sb = new StringBuilder(invocationsSize);
			sb.append(Integer.toBinaryString(++i));
			while(sb.length() < invocationsSize) {
				sb.insert(0, '0');
			}
			Set<MethodInvocation> envolvedInvocations = new HashSet<MethodInvocation>();
			List<ApiMethod> envolvedApiMethods = new ArrayList<ApiMethod>();
			List<String> envolvedSeeds = new ArrayList<String>();
			for (int j = 1; j <= sb.length(); j++) {
				if (sb.codePointAt(j-1) == '1') {
					MethodInvocation invocation = invocationsArray[invocationsSize-j];
					envolvedInvocations.add(invocation);
					envolvedApiMethods.add(this.mapInvocations.get(invocation));
					envolvedSeeds.add(invocation.toString());
				}
			}
			this.examples.add(makeExample(node, envolvedInvocations, envolvedApiMethods, envolvedSeeds));
		}while(!sb.toString().equals(new String(defaultBinaryNameArray)));
		
//		for (Set<ApiMethod> itemSet : this.setOfApiMethods) {
//			
//			List<ApiMethod> envolvedApiMethods = new ArrayList<ApiMethod>();
//			try {
//				for (ApiElement element : itemSet) {
//					envolvedApiMethods.add((ApiMethod) element);
//				}
//			} catch (Exception e) {
//				//FIXME Evaluate the occurrence of the throws on cast  
//				break;
//			}
//			
//			if (this.mapInvocations.values().containsAll(itemSet)) {
//				Set<MethodInvocation> envolvedInvocations = new HashSet<MethodInvocation>();
//				List<String> envolvedSeeds = new ArrayList<String>();
//				for ( Entry<MethodInvocation, ApiMethod> entry : this.mapInvocations.entrySet()) {
//					if (itemSet.contains(entry.getValue())) {
//						envolvedInvocations.add(entry.getKey());
//						envolvedSeeds.add(entry.getKey().toString());
//					}
//				}
//				
//				Example example = makeExample(node, envolvedInvocations, envolvedApiMethods, envolvedSeeds);
//				this.examples.add(example);
//			}
//		}
		
		
		
	}
	
	public Collection<Example> getExamples() {
		return examples;
	}

	public Collection<ApiClass> getApiClasses() {
		return this.classMap.values();
	}

	public long getSourceProjectId() {
		return sourceProjectId;
	}

	private Example makeExample(MethodDeclaration node, Set<MethodInvocation> envolvedInvocations,
			List<ApiMethod> envolvedApiMethods, List<String> envolvedSeeds) { 
 
		StatementVisitor visitor = new StatementVisitor(node, envolvedInvocations); 
		node.accept(visitor); 
		
		Collection<Statement> relatedStatements = visitor.getSliceStatementSet();  
		ASTNode newAST = ASTUtil.copyStatements(node.getBody(), relatedStatements, AST.newAST(AST.JLS3));
		if (!relatedStatements.isEmpty()) {
			LOGGER.error("Some Statements are not included!"); 
		}
		
		if (newAST == null) {
			LOGGER.error("Slicing process failed for node ");
			//TODO Se AST retornada for nula é porque faltou incluir statement(s)
		}else if (((Block) newAST).statements().isEmpty()) {
			LOGGER.error("Slicing process failed for node ");
			//TODO Se o Block retornado for vazio é porque faltou incluir statement(s)
		}
		
		ASTUtil.removeEmptyBlocks((Block) newAST);
		
		Example example = new Example();
		example.setAttachment(this.attachmentMap.get(node.getRoot()));
		example.setApiMethods(new HashSet<ApiElement>(envolvedApiMethods));
		example.setCodeExample(newAST.toString());
		example.setSeeds(envolvedSeeds);
		example.setSourceMethod(node.toString());
		
		IMethodBinding nodeBinding = node.resolveBinding();
		if (!this.methodMap.containsKey(nodeBinding)){
			ApiClass newApiClass = new ApiClass(node.resolveBinding().getDeclaringClass().getQualifiedName());
			methodDeclarationHandler(node, newApiClass);
		}
		example.setSourceMethodCall(this.methodMap.get(nodeBinding).getFullName());
		example.setAddedAt(new Date(System.currentTimeMillis()));
		
		try {
			example.setFormattedCodeExample(ASTUtil.codeFormatter(newAST.toString()));
		} catch (MalformedTreeException e) {
			LOGGER.error("Error on format the code.", e);
			//TODO Descobrir e tratar error na formatação do código
		} catch (BadLocationException e) {
			LOGGER.error("Error on format the code.", e);
			//TODO Descobrir e tratar error na formatação do código
		}
		
		example.getMetrics().put(ExampleMetric.LOC.name(), example.getFormattedCodeExample().split("\\.").length);
		example.getMetrics().put(ExampleMetric.DECISION_STATEMENTS.name(), visitor.getNumberOfDecisionStatements());
		example.getMetrics().put(ExampleMetric.INVOCATIONS.name(), visitor.getNumberOfInvocations());
		example.getMetrics().put(ExampleMetric.ARGUMENTS.name(), visitor.getNumberOfArguments());
		example.getMetrics().put(ExampleMetric.PRIMITIVE_ARGUMENTS.name(), visitor.getNumberOfPrimitiveArguments());
		example.getMetrics().put(ExampleMetric.NULL_ARGUMENTS.name(), visitor.getNumberOfNullArguments());
		example.getMetrics().put(ExampleMetric.FIELD_ARGUMENTS.name(), visitor.getNumberOfFieldArguments());
		example.getMetrics().put(ExampleMetric.UNDISCOVERED_DECLARATIONS.name(), visitor.getNumberOfUndiscoveredDeclarations());
		example.getMetrics().put(ExampleMetric.UNHANDLED_EXCEPTIONS.name(), visitor.getNumberOfUnhandledExceptions());
		
		return example;
	}
	
	private void methodDeclarationHandler(MethodDeclaration methodDeclaration, ApiClass apiClass){
		IMethodBinding mb = methodDeclaration.resolveBinding();
		if (mb == null) {
			return;
		}
		
		LinkedList<String> paramsType = new LinkedList<String>();
		for (ITypeBinding pt : mb.getParameterTypes()) {
			paramsType.add(pt.getQualifiedName().toString());
		}
		
		ApiMethod apiMethod = new ApiMethod(apiClass, mb.getName(), paramsType);
		this.setModifiers(apiMethod, methodDeclaration);
		
		if (mb.isConstructor()) {
			apiMethod.setConstructor(true);
			apiMethod.setVoid(false);
			apiMethod.setFunction(false);
			apiMethod.setReturnType(null);
		}else if (mb.getReturnType().getQualifiedName().equalsIgnoreCase("void")){
			apiMethod.setVoid(true);
			apiMethod.setFunction(false);
			apiMethod.setReturnType(null);
		}else{
			apiMethod.setVoid(false);
			apiMethod.setFunction(true);
			apiMethod.setReturnType(mb.getReturnType().getQualifiedName());
		}
		
		apiClass.getApiMethods().add(apiMethod);
		this.methodMap.put(mb, apiMethod);
		
	}

	@SuppressWarnings("unchecked")
	private void setModifiers(ApiElement apiElement, BodyDeclaration node) {
		
		for (Object o : node.modifiers()) {
			if (o instanceof Modifier){
				Modifier modifier = (Modifier) o;
				if (modifier.isAbstract()) {
					apiElement.setAbstract(true);
				}else if (modifier.isFinal()) {
					apiElement.setFinal(true);
				}else if (modifier.isPrivate()) {
					apiElement.setPrivate(true);
				}else if (modifier.isProtected()) {
					apiElement.setProtected(true);
				}else if (modifier.isPublic()) {
					apiElement.setPublic(true);
				}else if (modifier.isStatic()) {
					apiElement.setFinal(true);
				}
			}
		}
		apiElement.setDefault( !( apiElement.isPrivate() || apiElement.isPublic() || apiElement.isProtected() ) );
		
		Javadoc javadoc = node.getJavadoc();
		if (javadoc != null) {
			apiElement.setHasJavadoc(true);
			apiElement.setHidden(false);
			Stack<Object> tags = new Stack<Object>();
			tags.addAll(javadoc.tags());
			while (!tags.isEmpty()) {
				Object tag = tags.pop();
				if (tag instanceof TagElement) {
					String tagName = ((TagElement) tag).getTagName(); 
					if (tagName != null && tagName.equalsIgnoreCase("@hide")) {
						apiElement.setHidden(true);
						break;
					}
					tags.addAll(((TagElement) tag).fragments());
				}
			}
		}else{
			apiElement.setHasJavadoc(false);
			apiElement.setHidden(true);
		}
	}

	@Override
	public boolean visit(EnumDeclaration node){
		
		ITypeBinding typeBinding = node.resolveBinding();
		if (typeBinding != null) {
			ApiEnum apiClass = new ApiEnum(typeBinding.getQualifiedName());
			apiClass.setInterface(typeBinding.isInterface());
			this.setModifiers(apiClass, node);
			
			this.classMap.put(typeBinding, apiClass);
			
			if (!this.attachmentMap.containsKey(node.getRoot())) {
				Attachment attachment = new Attachment(typeBinding.getQualifiedName().concat(".java"), node.getRoot().toString().getBytes());
				this.attachmentMap.put(node.getRoot(), attachment);
			}
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		
		this.invocations = new HashSet<MethodInvocation>();
		this.mapInvocations = new HashMap<MethodInvocation, ApiMethod>();
		
		IMethodBinding methodBinding = node.resolveBinding();
		if (methodBinding != null) {
			ApiClass apiClass = this.classMap.get(methodBinding.getDeclaringClass());
			if (apiClass != null) {
				this.methodDeclarationHandler(node, apiClass);
			}else{
				//FIXME Evaluate the scenarios where this occurs 
			}
			
		}
		
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		
		String methodName = methodBinding.getName();
		String className = methodBinding.getDeclaringClass().getQualifiedName();
		List<String> params = new ArrayList<String>(methodBinding.getParameterTypes().length);
		for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
			params.add(i, methodBinding.getParameterTypes()[i].getQualifiedName());
		}
		
		ApiMethod apiMethod = new ApiMethodDAO().find(sourceProjectId, ApiMethod.parseFullName(className, methodName, params), DatabaseType.PRE_PROCESSING);
		if (apiMethod != null) {
			this.invocations.add(node);
			this.mapInvocations.put(node, apiMethod);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node){
		
		ITypeBinding typeBinding = node.resolveBinding();
		if (typeBinding != null) {
			ApiClass apiClass = new ApiClass(typeBinding.getQualifiedName());
			apiClass.setInterface(typeBinding.isInterface());
			this.setModifiers(apiClass, node);

			this.classMap.put(typeBinding, apiClass);
			
			if (!this.attachmentMap.containsKey(node.getRoot())) {
				Attachment attachment = new Attachment(typeBinding.getQualifiedName().concat(".java"), node.getRoot().toString().getBytes());
				this.attachmentMap.put(node.getRoot(), attachment);
			}
		}
		
		return super.visit(node);
	}
	
}
