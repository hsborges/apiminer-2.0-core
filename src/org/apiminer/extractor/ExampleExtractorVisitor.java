package org.apiminer.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.RuleDAO;
import org.apiminer.entities.Attachment;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiEnum;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.ExampleMetric;
import org.apiminer.entities.mining.Rule;
import org.apiminer.util.ASTUtil;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class ExampleExtractorVisitor extends ASTVisitor {

	private static final Logger LOGGER = Logger
			.getLogger(ExampleExtractorVisitor.class);

	private Map<Expression, ApiMethod> mapInvocations = new HashMap<Expression, ApiMethod>();

	private final long sourceProjectId;

	private final Collection<Example> examples = new ArrayList<Example>();

	private Map<ITypeBinding, ApiClass> classMap = new HashMap<ITypeBinding, ApiClass>();
	private Map<IMethodBinding, ApiMethod> methodMap = new HashMap<IMethodBinding, ApiMethod>();
	private Map<ASTNode, Attachment> attachmentMap = new HashMap<ASTNode, Attachment>();

	private TreeSet<Set<ApiMethod>> methodSets;
	
	/**
	 * @param sourceProjectId
	 *            Identificador da API na base de dados
	 * @throws IllegalArgumentException
	 *             Se o identificador do projeto for menor que 0
	 */
	public ExampleExtractorVisitor(long sourceProjectId) {
		super(true);
		if (sourceProjectId < 0) {
			throw new IllegalArgumentException(
					"Invalid source project identificator");
		} else {
			this.sourceProjectId = sourceProjectId;
		}
		
		this.methodSets = new TreeSet<Set<ApiMethod>>(new Comparator<Set<ApiMethod>>() {
			@Override
			public int compare(Set<ApiMethod> o1, Set<ApiMethod> o2) {
				if (o1.equals(o2)) {
					return 0;
				}else{
					int result = o1.size() - o2.size();
					if (result == 0) {
						return -1;
					}else{
						return result;
					}
				}
			}
		});
		
		Stack<Rule> rules = new Stack<Rule>();
		rules.addAll(new RuleDAO().findAll());
		while (!rules.isEmpty()) {
			Rule r = rules.pop();
			Set<ApiMethod> methods = new HashSet<ApiMethod>();
			methods.addAll(r.getPremise().getElements());
			methods.addAll(r.getConsequence().getElements());
			this.methodSets.add(methods);
		}
		
	}
	
	public ExampleExtractorVisitor(long sourceProjectId, Collection<Set<ApiMethod>> sets){
		
		super(true);
		if (sourceProjectId < 0) {
			throw new IllegalArgumentException(
					"Invalid source project identificator");
		} else {
			this.sourceProjectId = sourceProjectId;
		}
		
		this.methodSets = new TreeSet<Set<ApiMethod>>(new Comparator<Set<ApiMethod>>() {
			@Override
			public int compare(Set<ApiMethod> o1, Set<ApiMethod> o2) {
				return o1.size() - o2.size();
			}
		});
		
		this.methodSets.addAll(sets);
		
	}

	private Example makeExample(MethodDeclaration node,
			Set<? extends Expression> envolvedInvocations,
			List<ApiMethod> envolvedApiMethods) {

		//  Visitor responsável por realizar o slicing de programas
		SlicingStatementVisitor visitor = new SlicingStatementVisitor(node, new HashSet<ASTNode>(envolvedInvocations));
		node.accept(visitor);  

		Collection<Statement> relatedStatements = visitor.getSlicedStatements(); 
		
		ASTNode newAST = ASTUtil.copyStatements(node.getBody(), relatedStatements, AST.newAST(AST.JLS3));
		if (!relatedStatements.isEmpty()) {
			LOGGER.error("Some Statements are not included!");
		}

		if (newAST == null) { 
			LOGGER.error("Slicing process failed for node ");
			// TODO Se AST retornada for nula é porque faltou incluir statement(s)
			return null;
		} else if (((Block) newAST).statements().isEmpty()) {
			LOGGER.error("Slicing process failed for node ");
			// TODO Se o Block retornado for vazio é porque faltou incluir statement(s)
			return null;
		}

		
		ASTUtil.removeEmptyBlocks((Block) newAST);

		// Adiciona declarações de variáveis que não foram encontradas no escopo do método
		// Para facilitar, tipos iguais são declarados no mesmo Statement
		Set<String> additionalDeclarationLines = new HashSet<String>();
		Map<ITypeBinding, List<IVariableBinding>> typesMap = new HashMap<ITypeBinding, List<IVariableBinding>>();
		for (IVariableBinding ivb : visitor.getUndiscoveredDeclarations()) {
			if (!typesMap.containsKey(ivb.getType())) {
				typesMap.put(ivb.getType(), new ArrayList<IVariableBinding>(2));
			}
			typesMap.get(ivb.getType()).add(ivb);
		}
		
		for (ITypeBinding typeBinding : typesMap.keySet()) {
			List<IVariableBinding> variableBindings = typesMap.get(typeBinding);
			
			Stack<VariableDeclarationFragment> fragments = new Stack<VariableDeclarationFragment>();
			for (IVariableBinding ivb : variableBindings) {
				VariableDeclarationFragment declarationFragment = newAST.getAST().newVariableDeclarationFragment();
				declarationFragment.setName(newAST.getAST().newSimpleName(ivb.getName()));
				fragments.add(declarationFragment);
			}
			
			VariableDeclarationStatement statement = newAST.getAST().newVariableDeclarationStatement(fragments.pop());
			while(!fragments.isEmpty()){
				statement.fragments().add(fragments.pop());
			}
			
			statement.setType(this.getType(typeBinding, newAST.getAST()));
			
			additionalDeclarationLines.add(statement.toString());
			((Block) newAST).statements().add(0, statement);
		}
		
		Example example = new Example();
		example.setAttachment(this.attachmentMap.get(node.getRoot()));
		example.setApiMethods(new HashSet<ApiMethod>(envolvedApiMethods));
		example.setImports(visitor.getImports());
		
		for (Expression seed : envolvedInvocations) {
			example.getSeeds().add(seed.toString());
		}
		
		example.setSourceMethod(node.toString());
		example.setAddedAt(new Date(System.currentTimeMillis()));
		
		try {
			IMethodBinding nodeBinding = node.resolveBinding();
			if (!this.methodMap.containsKey(nodeBinding)) {
				ApiClass newApiClass = new ApiClass(nodeBinding.getDeclaringClass().getQualifiedName());
				methodDeclarationHandler(node, newApiClass);
			}
			example.setSourceMethodCall(this.methodMap.get(nodeBinding).getFullName());
		} catch (Exception e) {
			LOGGER.error(e);
			if (example.getSourceMethodCall() == null) {
				example.setSourceMethodCall("?");
			}
		}
		
		String codeExample = newAST.toString();
		for (String line : additionalDeclarationLines) {
			codeExample = codeExample.replace(line, line.replace("\n", "").concat("  ").concat("//initialized previously").concat("\n"));
		}
		
		try {
			example.setCodeExample(codeExample);
			example.setFormattedCodeExample(ASTUtil.codeFormatter(codeExample));
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
			if (example.getFormattedCodeExample() == null) {
				example.setFormattedCodeExample(codeExample);
			}
		}
		
		//TODO Obter métricas do exemplo
		example.getMetrics().put(ExampleMetric.LOC.name(), example.getFormattedCodeExample().split("\n").length -1 );
		example.getMetrics().put(ExampleMetric.ARGUMENTS.name(), visitor.getNumberOfArguments());
		example.getMetrics().put(ExampleMetric.DECISION_STATEMENTS.name(), visitor.getNumberOfDecisionStatements());
		example.getMetrics().put(ExampleMetric.INVOCATIONS.name(), visitor.getNumberOfInvocations());
		example.getMetrics().put(ExampleMetric.NULL_ARGUMENTS.name(), visitor.getNumberOfNullArguments());
		example.getMetrics().put(ExampleMetric.PRIMITIVE_ARGUMENTS.name(), visitor.getNumberOfPrimitiveArguments());
		example.getMetrics().put(ExampleMetric.FIELD_ARGUMENTS.name(), visitor.getNumberOfFieldArguments());
		example.getMetrics().put(ExampleMetric.UNDISCOVERED_DECLARATIONS.name(), visitor.getNumberOfUndiscoveredDeclarations());
		example.getMetrics().put(ExampleMetric.UNHANDLED_EXCEPTIONS.name(), visitor.getNumberOfUnhandledExceptions());

		return example;
	}
	
	// Função auxiliar para copiar o tipo da variável
	// considerando que um tipo nao pode pertencer a outra AST
	private Type getType(ITypeBinding typeBinding, AST newAST){
		if (typeBinding.isPrimitive()) {
			return newAST.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
		}else if(typeBinding.isArray()){
			return newAST.newArrayType(this.getType(typeBinding.getElementType(), newAST), typeBinding.getDimensions());
		}else if(typeBinding.isParameterizedType()){
			ParameterizedType pt = newAST.newParameterizedType(this.getType(typeBinding.getTypeDeclaration(), newAST));
			for (ITypeBinding itb : typeBinding.getTypeArguments()) {
				pt.typeArguments().add(this.getType(itb, newAST));
			}
			return pt;
		}else if(typeBinding.isWildcardType()){
			WildcardType wt = newAST.newWildcardType();
			wt.setBound(typeBinding.getBound() == null ? null : this.getType(typeBinding.getBound(), newAST), typeBinding.isUpperbound());
			return wt;
		}else{
			try {
				return newAST.newSimpleType(newAST.newName(typeBinding.getQualifiedName()));
			} catch (Exception e) {
				return newAST.newSimpleType(newAST.newName(typeBinding.getName()));
			} 
		}
	}

	// Metodo para tratamento de declarações de métodos
	private void methodDeclarationHandler(MethodDeclaration methodDeclaration, ApiClass apiClass) {
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
		} else if (mb.getReturnType().getQualifiedName()
				.equalsIgnoreCase("void")) {
			apiMethod.setVoid(true);
			apiMethod.setFunction(false);
			apiMethod.setReturnType(null);
		} else {
			apiMethod.setVoid(false);
			apiMethod.setFunction(true);
			apiMethod.setReturnType(mb.getReturnType().getQualifiedName());
		}

		apiClass.getApiMethods().add(apiMethod);
		this.methodMap.put(mb, apiMethod);

	}

	// Método para configurar os modificadores
	@SuppressWarnings("unchecked")
	private void setModifiers(ApiElement apiElement, BodyDeclaration node) {

		for (Object o : node.modifiers()) {
			if (o instanceof Modifier) {
				Modifier modifier = (Modifier) o;
				if (modifier.isAbstract()) {
					apiElement.setAbstract(true);
				} else if (modifier.isFinal()) {
					apiElement.setFinal(true);
				} else if (modifier.isPrivate()) {
					apiElement.setPrivate(true);
				} else if (modifier.isProtected()) {
					apiElement.setProtected(true);
				} else if (modifier.isPublic()) {
					apiElement.setPublic(true);
				} else if (modifier.isStatic()) {
					apiElement.setFinal(true);
				}
			}
		}
		
		apiElement.setDefault(!(apiElement.isPrivate() || apiElement.isPublic() || apiElement.isProtected()));

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
		} else {
			apiElement.setHasJavadoc(false);
			apiElement.setHidden(true);
		}
		
	}

	@Override
	public boolean visit(EnumDeclaration node) {

		if (node.isPackageMemberTypeDeclaration() || node.isMemberTypeDeclaration() ) {
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
				
				List<MethodDeclaration> declarations = new LinkedList<MethodDeclaration>();
				for (Object o : node.bodyDeclarations()) {
					if (o instanceof MethodDeclaration) {
						declarations.add((MethodDeclaration) o);
					}
				}
				
				for (final MethodDeclaration declaration : declarations) {
					
					final List<Expression> invocations = new LinkedList<Expression>();
					this.methodDeclarationHandler(declaration, apiClass);
					
					InvocationVisitor visitor = new InvocationVisitor();
					declaration.accept(visitor);
					
					invocations.addAll(visitor.invocations);
					
					// Parte de extracao de exemplos, se não houver invocações eu pulo esta parte
					if (invocations.isEmpty()) {
						return super.visit(node);
					}else{
						// Faço a extração de exemplos para cada invocação em separado
						Set<ApiMethod> methodsInvocations = new HashSet<ApiMethod>();
						for (Expression mi : invocations) {
							methodsInvocations.add(this.mapInvocations.get(mi));
							Example newExample = makeExample(declaration, 
									Collections.singleton(mi), 
									Collections.singletonList(this.mapInvocations.get(mi)));
							if (newExample != null) {
								this.examples.add(newExample);
							}
						}
						
						// Se houver mais de uma invocacao faço o slicing com sementes multiplas
						if (invocations.size() > 1) {
							// Itero sobre os conjuntos das regras
							Iterator<Set<ApiMethod>> it = this.methodSets.iterator();
							while (it.hasNext()) {
								// Podando conjuntos
								Set<ApiMethod> s = it.next();
								if (s.size() > methodsInvocations.size()) {
									break;
								}else{
									// Se as invocacoes identificadas envolvem metodos desejados eu processo
									if (methodsInvocations.containsAll(s)) {
										List<ApiMethod> methods = new ArrayList<ApiMethod>();
										Set<Expression> invocationsTemp = new HashSet<Expression>();
										for (Expression mi : invocations) {
											ApiMethod relatedMethod = this.mapInvocations.get(mi); 
											if (s.contains(relatedMethod)) {
												methods.add(relatedMethod);
												invocationsTemp.add(mi);
											}
										}
										Example newExample = makeExample(declaration, invocationsTemp, methods);
										if (newExample != null) {
											this.examples.add(newExample);
										}
									}
								}
							}
						}
						
					}
				}
				
			}
		}

		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		
		if (node.isPackageMemberTypeDeclaration() || node.isMemberTypeDeclaration() ) {
			ITypeBinding typeBinding = node.resolveBinding();
			if (typeBinding != null) {
				// Criando classe
				ApiClass apiClass = new ApiClass(typeBinding.getQualifiedName());
				apiClass.setInterface(typeBinding.isInterface());
				this.setModifiers(apiClass, node);

				this.classMap.put(typeBinding, apiClass);

				if (!this.attachmentMap.containsKey(node.getRoot())) {
					Attachment attachment = new Attachment(typeBinding
							.getQualifiedName().concat(".java"), node.getRoot()
							.toString().getBytes());
					this.attachmentMap.put(node.getRoot(), attachment);
				}
				
				List<MethodDeclaration> declarations = new LinkedList<MethodDeclaration>();
				for (Object o : node.bodyDeclarations()) {
					if (o instanceof MethodDeclaration) {
						declarations.add((MethodDeclaration) o);
					}
				}

				for (final MethodDeclaration declaration : declarations) {
					
					final List<Expression> invocations = new LinkedList<Expression>();
					this.methodDeclarationHandler(declaration, apiClass);
					
					InvocationVisitor visitor = new InvocationVisitor();
					declaration.accept(visitor);
					
					invocations.addAll(visitor.invocations);
					
					// Parte de extracao de exemplos, se não houver invocações eu pulo esta parte
					if (invocations.isEmpty()) {
						continue;
					}else{
						// Faço a extração de exemplos para cada invocação em separado
						Set<ApiMethod> methodsInvocations = new HashSet<ApiMethod>();
						for (Expression mi : invocations) {
							methodsInvocations.add(this.mapInvocations.get(mi)); 
							Example newExample = makeExample(declaration, 
									Collections.singleton(mi), 
									Collections.singletonList(this.mapInvocations.get(mi)));
							if (newExample != null) {
								this.examples.add(newExample);
							}
						}
						
						// Se houver mais de uma invocacao faço o slicing com sementes multiplas
						if (invocations.size() > 1) {
							// Itero sobre os conjuntos das regras
							Iterator<Set<ApiMethod>> it = this.methodSets.iterator();
							while (it.hasNext()) {
								// Podando conjuntos
								Set<ApiMethod> s = it.next(); 
								if (s.size() > methodsInvocations.size()) {
									break;
								}else{
									// Se as invocacoes identificadas envolvem metodos desejados eu processo
									if (methodsInvocations.containsAll(s)) {
										List<ApiMethod> methods = new ArrayList<ApiMethod>();
										Set<Expression> invocationsTemp = new HashSet<Expression>();
										for (Expression mi : invocations) {
											ApiMethod relatedMethod = this.mapInvocations.get(mi); 
											if (s.contains(relatedMethod)) {
												methods.add(relatedMethod);
												invocationsTemp.add(mi);
											}
										}
										Example newExample = makeExample(declaration, invocationsTemp, methods);
										if (newExample != null) {
											this.examples.add(newExample);
										}
									}
								}
							}
						}
						
					}
				}
				
				
			}
		}

		return super.visit(node);
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
	
	private class InvocationVisitor extends ASTVisitor {
		
		private List<Expression> invocations = new LinkedList<Expression>();
		
		@Override
		public boolean visit(MethodInvocation methodInvocation) {
			// Se não for possível fazer o binding, este método não será da API analisada
			// já que a api foi incluida. Além disse as informações obtidas a partir desse 
			// método não são precisas e as vezes são incorretas
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding != null) {
				invocationHandler(methodBinding, methodInvocation);
			}
			return super.visit(methodInvocation);
		}

		@Override
		public boolean visit(ClassInstanceCreation node) {
			// Se não for possível fazer o binding, este método não será da API analisada
			// já que a api foi incluida. Além disse as informações obtidas a partir desse 
			// método não são precisas e as vezes são incorretas
			IMethodBinding methodBinding = node.resolveConstructorBinding();
			if (methodBinding != null) {
				invocationHandler(methodBinding, node);
			}
			return super.visit(node);
		}
		
		private void invocationHandler(IMethodBinding methodBinding, Expression invocation){
			// Obtenho informações da invocação, como nome do metodo, classe e argumentos
			String methodName = methodBinding.getName();
			String className = methodBinding.getDeclaringClass().getQualifiedName();
			List<String> params = new ArrayList<String>(methodBinding.getParameterTypes().length);
			for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
				params.add(i,methodBinding.getParameterTypes()[i].getQualifiedName());
			}

			// Busca na base por informações do método
			ApiMethod apiMethod = new ApiMethodDAO().find(sourceProjectId, ApiMethod.parseFullName(className, methodName, params), DatabaseType.PRE_PROCESSING);

			// Se o método esta presente na API analisada, esta invocação é incluída
			if (apiMethod != null) {
				this.invocations.add(invocation);
				mapInvocations.put(invocation, apiMethod);
			}
		}
		
	}
	
}
