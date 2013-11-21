package org.apiminer.extractor.old;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apiminer.util.ASTUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Visitor que extende {@link ASTVisitor} e tem por objetivo analisar todos os 
 * {@link Statement} e incluir aqueles que são parte do slice.
 * 
 *  <p>
 *  Ressaltando que a coleta dos {@link Statement} é realizada intra-método.
 *  
 *  <p>
 *  Este visitor também coleta informações importantes para extração de métricas 
 *  dos exemplos.
 * 
 * @author Hudson S. Borges
 *
 */
/**
 * @author hudson
 *
 */
public class StatementVisitor extends ASTVisitor {

	/**
	 * {@link Statement} inclusos no slice 
	 */
	private HashSet<Statement> sliceStatementSet = new HashSet<Statement>();
	
	/**
	 * 	Variaveis de declaração identificadas
	 */
	private HashSet<IBinding> declarationVariableSet = null;
	
	/**
	 * 	Variáveis de atribuição identificadas
	 */
	private HashSet<IVariableBinding> assignmentVariableSet = null;
	
	/**
	 * Mapeamento entre a invocação e as variaveis envolvidas
	 */
	private HashMap<MethodInvocation, Collection<IVariableBinding>> envolvedVariableMap = null;
	
	/**
	 * Mapeamento entre invocação e variável de retorno
	 */
	private HashMap<MethodInvocation, IVariableBinding> returnedVariableMap = null;
	
	/**
	 * Conjunto de todas as variáveis de retorno envolvidos 
	 */
	private Set<IVariableBinding> returnedVariableSet = null;
	
	
	// Variáveis para calculo de métricas
	private int numberOfDecisionStatements = 0;
	private int numberOfInvocations = 0;
	private int numberOfArguments = 0;
	private int numberOfPrimitiveArguments = 0;
	private int numberOfNullArguments = 0;
	private int numberOfFieldArguments = 0;
	private int numberOfUndiscoveredDeclarations = 0;
	private int numberOfUnhandledExceptions = 0;
	
	public StatementVisitor(ASTNode startFromNode, Collection<MethodInvocation> invocations){
		
		if (startFromNode == null || invocations == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}else if (invocations.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		// Instancia as variáveis
		this.assignmentVariableSet = new HashSet<IVariableBinding>();
		this.declarationVariableSet = new HashSet<IBinding>();
		this.returnedVariableSet = new HashSet<IVariableBinding>();
		
		this.envolvedVariableMap = new HashMap<MethodInvocation, Collection<IVariableBinding>>();
		this.returnedVariableMap = new HashMap<MethodInvocation, IVariableBinding>();
		
		// Coleta as variaveis relacionadas de cada invocação 
		for (MethodInvocation mi : invocations) {
			Set<IVariableBinding> invocationVariables = ASTUtil.collectVariables(mi);
			
			this.envolvedVariableMap.put(mi, invocationVariables);
			this.declarationVariableSet.addAll(invocationVariables);
			this.assignmentVariableSet.addAll(invocationVariables);
			
			// Obtem a variável de retorno, se este é uma função
			if (!mi.resolveMethodBinding().getReturnType().isPrimitive()){
				Name r = ASTUtil.getReturnVariable(mi);
				if (r != null) {
					IBinding resolved = r.resolveBinding();
					if (resolved instanceof IVariableBinding) {
						this.returnedVariableMap.put(mi, (IVariableBinding) resolved);
						this.returnedVariableSet.add((IVariableBinding) resolved);
					}
				}
			}
	
			// Obtem o primeiro Statement pai
			this.sliceStatementSet.add((Statement) ASTUtil.getParent(mi, Statement.class));
			for (IBinding binding : this.declarationVariableSet) {   
				Statement declaration = ASTUtil.getDeclarationStatement(binding, startFromNode);
				if (declaration != null) {
					this.sliceStatementSet.add(declaration);
				}
			}
			
			//TODO This is needed? Evaluate another Statements was WhileStatement, etc ...
			ASTNode logicalDependecy = ASTUtil.getParent(mi, IfStatement.class, startFromNode);
			if (logicalDependecy != null) {
				IfStatement ifStatement = (IfStatement) logicalDependecy;
				Set<IVariableBinding> variablesLogicalEnvolved = ASTUtil.collectVariables(ifStatement.getExpression());
				
				this.sliceStatementSet.add(ifStatement);
				this.declarationVariableSet.addAll(variablesLogicalEnvolved);
				this.assignmentVariableSet.addAll(variablesLogicalEnvolved);
			}
			
			//Extrai informações estatisticas do metodo
			this.numberOfInvocations++;
			for (Object o : mi.arguments()) {
				this.numberOfArguments++;
				this.numberOfUndiscoveredDeclarations++;
				Expression expression = (Expression) o;
				ITypeBinding argumentBinding = expression.resolveTypeBinding();
				
				if (argumentBinding == null) {
					//FIXME Tratar variáveis não identificadas
//					System.out.println(String.format("%s \n -=-",expression));
					continue;
				}
				
				// Se o argumento é um tipo primitivo
				if (argumentBinding.isPrimitive()){
					numberOfPrimitiveArguments++;
					numberOfUndiscoveredDeclarations--;
				}
				
				// Se é um argumento nulo
				if (argumentBinding.isNullType()){
					numberOfNullArguments++;
					numberOfUndiscoveredDeclarations--;
				}
				
				// Verifica se o argumento é uma variável de classe
				if (argumentBinding.isTypeVariable()){
					IVariableBinding vb = (IVariableBinding) argumentBinding;
					if (vb.isField()){
						numberOfFieldArguments ++;
						numberOfUndiscoveredDeclarations--;
					}	
				}
			}
			
			// Verifica se o método lança excessões e não é tratado no código
			if(mi.resolveMethodBinding().getExceptionTypes().length > 0 && ASTUtil.getParent(mi, TryStatement.class) == null)
				numberOfUnhandledExceptions++;
				
			
		}
	}
	
	@Override
	public void endVisit(AssertStatement node) {
		//TODO Evaluate the need of handle the message expression for this node
		if (include(node.getExpression(), node)){
			numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(Block node) {
		// This node type isn't needed
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(BreakStatement node) {
		// This node type isn't needed
		super.endVisit(node); 
	}
	
	@Override
	public void endVisit(ConstructorInvocation node) {
		// This node type isn't needed
		super.endVisit(node); 
	}
	
	@Override
	public void endVisit(ContinueStatement node) {
		// This node type isn't needed
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(DoStatement node) {
		if ( include(node.getExpression(), node) ) {
			numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(EmptyStatement node) {
		// This node type isn't needed
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(EnhancedForStatement node) {
		if (include(node.getExpression(), node)){
			numberOfDecisionStatements++;
		}
		
		IBinding binding = node.getParameter().getName().resolveBinding();
		if (binding != null && binding instanceof IVariableBinding) {
			if (this.assignmentVariableSet.contains(binding)) {
				this.sliceStatementSet.add(node);
				this.assignmentVariableSet.remove(binding);
			}
		}
		
		super.endVisit(node);
	}
	
	
	/*
	 *	Simples Statement que faz a ligação entre um Statement e um Expression
	 *  Nesta parte identifico se o Expression está relacionado as variáveis relacionadas 
	 */
	@Override
	public void endVisit(ExpressionStatement node) {
		include(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ForStatement node) {
		if (node.getExpression() != null) {
			if(include(node.getExpression(), node)){
				numberOfDecisionStatements++;
			}
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(IfStatement node) {
		if(include(node.getExpression(), node)){
			numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(LabeledStatement node) {
		include(node.getLabel(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ReturnStatement node) {
		include(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SuperConstructorInvocation node) {
		include(node.getExpression(), node);
		for (Object o : node.arguments()) {
			include((Expression)o, node); 
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SwitchCase node) {
		// This node type isn't needed
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SwitchStatement node) {
		if(include(node.getExpression(), node)){
			numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SynchronizedStatement node) {
		include(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ThrowStatement node) {
		include(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(TryStatement node) {
		// This node type isn't needed
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(TypeDeclarationStatement node) {
		//TODO Implement this type is very important
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		for (Object o : node.fragments()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) o;
			IBinding nameBinding = fragment.getName().resolveBinding();
			if (this.assignmentVariableSet.contains(nameBinding)) {
				this.sliceStatementSet.add(node);
				this.assignmentVariableSet.remove(nameBinding);
			}
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(WhileStatement node) {
		if(include(node.getExpression(), node)){
			numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}

	private final boolean include(Expression node, Statement statement){
		
		if (node == null) {
			return false;
		}else if (statement == null) {
			throw new IllegalArgumentException("Statement must be not null");
		}
		
		boolean included = false;
		
		// Set of variables of reading, they should be after the method call
		Set<IVariableBinding> readVariables = ASTUtil.collectReadableVariables(node);

		// Set of variables of write, they should be before the method call
		Set<IVariableBinding> writeVariables = ASTUtil.collectWritableVariables(node);


		if (!readVariables.isEmpty()) {
			for (MethodInvocation mi : this.returnedVariableMap.keySet()) {
				if (node.getStartPosition() > mi.getStartPosition()) {
					if (readVariables.contains(this.returnedVariableMap.get(mi))) {
						this.sliceStatementSet.add(statement);
						this.returnedVariableSet.remove(this.returnedVariableMap.get(mi));
						included = true;
					}
				}
			}
		}

		if (!writeVariables.isEmpty()) {
			for (MethodInvocation mi : this.envolvedVariableMap.keySet()) {
				if (node.getStartPosition() < mi.getStartPosition()) {
					for (IVariableBinding variableBinding : writeVariables) {
						if (this.envolvedVariableMap.get(mi).contains(variableBinding)) {
							if (this.assignmentVariableSet.contains(variableBinding)){
								this.sliceStatementSet.add(statement);
								this.assignmentVariableSet.remove(variableBinding);
								included = true;
							}
						}
					}
				}
			}
		}
		
		return included;
		
	}
	
	public HashSet<Statement> getSliceStatementSet() {
		return sliceStatementSet;
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		Statement superStatement = (Statement) ASTUtil.getParent(node, Statement.class);
		for (Object o : node.arguments()) {
			include( (Expression) o, superStatement);
		}
		if (node.getExpression() != null) {
			include(node.getExpression(), superStatement);
		}
		if (node.getAnonymousClassDeclaration() != null){
			for (Object o : node.getAnonymousClassDeclaration().bodyDeclarations()) {
				BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
				if (bodyDeclaration instanceof Initializer) {
					Initializer init = (Initializer) bodyDeclaration;
					if (!Collections.disjoint(init.getBody().statements(), this.sliceStatementSet)) {
						this.sliceStatementSet.add((Statement) ASTUtil.getParent(node, Statement.class));
					}
				}else if (bodyDeclaration instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration) bodyDeclaration;
					if (!Collections.disjoint(md.getBody().statements(), this.sliceStatementSet)) {
						this.sliceStatementSet.add(superStatement);
					}
				}else{
					//Do nothing
				}
			}
		}
		super.endVisit(node);
	}

	public int getNumberOfDecisionStatements() {
		return numberOfDecisionStatements;
	}

	public HashSet<IBinding> getDeclarationVariableSet() {
		return declarationVariableSet;
	}

	public HashSet<IVariableBinding> getAssignmentVariableSet() {
		return assignmentVariableSet;
	}

	public HashMap<MethodInvocation, Collection<IVariableBinding>> getEnvolvedVariableMap() {
		return envolvedVariableMap;
	}

	public HashMap<MethodInvocation, IVariableBinding> getReturnedVariableMap() {
		return returnedVariableMap;
	}

	public Set<IVariableBinding> getReturnedVariableSet() {
		return returnedVariableSet;
	}

	public int getNumberOfInvocations() {
		return numberOfInvocations;
	}

	public int getNumberOfArguments() {
		return numberOfArguments;
	}

	public int getNumberOfPrimitiveArguments() {
		return numberOfPrimitiveArguments;
	}

	public int getNumberOfNullArguments() {
		return numberOfNullArguments;
	}

	public int getNumberOfFieldArguments() {
		return numberOfFieldArguments;
	}

	public int getNumberOfUndiscoveredDeclarations() {
		return numberOfUndiscoveredDeclarations;
	}

	public int getNumberOfUnhandledExceptions() {
		return numberOfUnhandledExceptions;
	}
	
	

}

