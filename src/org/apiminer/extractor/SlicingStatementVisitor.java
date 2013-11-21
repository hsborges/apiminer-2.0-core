package org.apiminer.extractor;

import java.util.Collection;
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
import org.eclipse.jdt.core.dom.ImportDeclaration;
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
public class SlicingStatementVisitor extends ASTVisitor {

	private final HashSet<Statement> slicedStatements = new HashSet<Statement>();
	
	private HashMap<ASTNode, Collection<IVariableBinding>> writableVariablesByNode = null;
	private HashMap<ASTNode, IVariableBinding> readableVariablesByNode = null;
	
	private Set<IVariableBinding> writableVariables = null;
	private Set<IVariableBinding> readableVariables = null;
	
	private Set<IVariableBinding> undiscoveredDeclarations = null;
	
	private ASTNode startNode = null;
	
	// Variáveis para calculo de métricas
	private int numberOfDecisionStatements = 0;
	private int numberOfInvocations = 0;
	private int numberOfArguments = 0;
	private int numberOfPrimitiveArguments = 0;
	private int numberOfNullArguments = 0;
	private int numberOfFieldArguments = 0;
	private int numberOfUndiscoveredDeclarations = 0;
	private int numberOfUnhandledExceptions = 0;
	
	private Set<String> imports;
	
	public SlicingStatementVisitor(ASTNode startFromNode, Collection<ASTNode> seeds) {
		
		if (startFromNode == null || seeds == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}else if (seeds.isEmpty()) {
			throw new IllegalArgumentException();
		}
		
		this.startNode = startFromNode;
		
		// Instancia as variáveis
		this.readableVariables = new HashSet<IVariableBinding>();
		this.readableVariablesByNode = new HashMap<ASTNode, IVariableBinding>();
		this.writableVariables = new HashSet<IVariableBinding>();
		this.writableVariablesByNode = new HashMap<ASTNode, Collection<IVariableBinding>>();
		this.undiscoveredDeclarations = new HashSet<IVariableBinding>();
		
		// Coleta as variaveis relacionadas de cada invocação 
		for (ASTNode node : seeds) {
			
			Set<IVariableBinding> writableVariables = ASTUtil.collectVariables2(node);
			this.writableVariables.addAll(writableVariables);
			this.undiscoveredDeclarations.addAll(writableVariables);
			this.writableVariablesByNode.put(node, writableVariables);
			
			// Obtem a variável de retorno, se este é uma invocação de método
			if (node instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) node; 
				if (!mi.resolveMethodBinding().getReturnType().isPrimitive()){
					Name r = ASTUtil.getReturnVariable((MethodInvocation) node);
					if (r != null) {
						IBinding resolved = r.resolveBinding();
						if (resolved instanceof IVariableBinding) {
							this.readableVariablesByNode.put(node, (IVariableBinding) resolved);
							this.readableVariables.add((IVariableBinding) resolved);
						}
					}
				}
				
				//Extrai informações estatisticas do metodo
				this.numberOfInvocations++;
				for (Object o : mi.arguments()) {
					this.numberOfArguments++;
					this.numberOfUndiscoveredDeclarations++;
					Expression expression = (Expression) o;
					ITypeBinding argumentBinding = expression.resolveTypeBinding();
					
					if (argumentBinding == null) {
						continue;
					}
					
					// Se o argumento é um tipo primitivo
					if (argumentBinding.isPrimitive()){
						this.numberOfPrimitiveArguments++;
						this.numberOfUndiscoveredDeclarations--;
					}
					
					// Se é um argumento nulo
					if (argumentBinding.isNullType()){
						this.numberOfNullArguments++;
						this.numberOfUndiscoveredDeclarations--;
					}
					
					// Verifica se o argumento é uma variável de classe
					if (!argumentBinding.isPrimitive() && !argumentBinding.isNullType()){
						this.numberOfFieldArguments ++;
						this.numberOfUndiscoveredDeclarations--;
					}
				}
				
				// Verifica se o método lança excessões e não é tratado no código
				if(mi.resolveMethodBinding().getExceptionTypes().length > 0 && ASTUtil.getParent(mi, TryStatement.class) == null)
					numberOfUnhandledExceptions++;
				
			}
	
			// Já adiciona o Statement o nó e procura pelos Statements de declaralçao das variáveis envolvidas
			this.slicedStatements.add((Statement) ASTUtil.getParent(node, Statement.class));
			for (IBinding binding : this.writableVariables) {   
				Statement declaration = ASTUtil.getDeclarationStatement(binding, startFromNode);
				if (declaration != null) {
					this.slicedStatements.add(declaration);
					this.undiscoveredDeclarations.remove(binding);
				}
			}
			
			//TODO Isto é necessário? Avaliar outros Statements como while, for, etc ...
			//TODO Avaliar também o nível que o IfStatement pode estar
			ASTNode logicalDependecy = ASTUtil.getParent(node, IfStatement.class, 1);
			if (logicalDependecy != null) {
				IfStatement ifStatement = (IfStatement) logicalDependecy;
				Set<IVariableBinding> newWritableVariables = ASTUtil.collectVariables2(ifStatement.getExpression());
				this.slicedStatements.add(ifStatement);
				this.writableVariables.addAll(newWritableVariables);
				this.writableVariablesByNode.put(ifStatement, newWritableVariables);
			}
			
		}
	}
	
	@Override
	public void endVisit(AssertStatement node) {
		//TODO Evaluate the need of handle the message expression for this node
		this.decide(node.getExpression(), node);
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
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
		this.decide(node.getExpression(), node);
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
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
		this.decide(node.getExpression(), node);
		IVariableBinding bind = node.getParameter().resolveBinding();
		if (bind != null){
			if (this.writableVariables.contains(bind)) {
				this.slicedStatements.add(node);
				this.undiscoveredDeclarations.remove(bind);
				Set<IVariableBinding> v = ASTUtil.collectReadableVariables(node.getExpression());
				v.removeAll(this.writableVariables);
				v.removeAll(this.readableVariables);
				this.undiscoveredDeclarations.addAll(v);
			}
		}
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	
	/*
	 *	Simples Statement que faz a ligação entre um Statement e um Expression
	 *  Nesta parte identifico se o Expression está relacionado as variáveis relacionadas 
	 */
	@Override
	public void endVisit(ExpressionStatement node) {
		this.decide(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ForStatement node) {
		if (node.getExpression() != null) {
			this.decide(node.getExpression(), node);
		}
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
			for (Object o : node.updaters()) {
				Set<IVariableBinding> v = ASTUtil.collectReadableVariables((Expression)o);
				v.removeAll(this.writableVariables);
				v.removeAll(this.readableVariables);
				this.undiscoveredDeclarations.addAll(v);
				v = ASTUtil.collectWritableVariables((Expression)o);
			}
			for (Object o : node.initializers()) {
				this.undiscoveredDeclarations.removeAll(ASTUtil.collectVariables2((Expression)o));
			}
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(IfStatement node) {
		this.decide(node.getExpression(), node);
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(LabeledStatement node) {
		this.decide(node.getLabel(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ReturnStatement node) {
		this.decide(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SuperConstructorInvocation node) {
		this.decide(node.getExpression(), node);
		for (Object o : node.arguments()) {
			decide((Expression)o, node); 
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
		this.decide(node.getExpression(), node);
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(SynchronizedStatement node) {
		this.decide(node.getExpression(), node);
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ThrowStatement node) {
		this.decide(node.getExpression(), node);
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
			if (this.writableVariables.contains(nameBinding)) {
				this.slicedStatements.add(node);
			}
		}
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(WhileStatement node) {
		this.decide(node.getExpression(), node);
		if (this.slicedStatements.contains(node)) {
			this.numberOfDecisionStatements++;
		}
		super.endVisit(node);
	}

	private void decide(Expression node, Statement statement){
		
		if (node == null) {
			return;
		}else if (statement == null) {
			throw new IllegalArgumentException("Statement must be not null");
		}
		
		// Conjunto de variáveis de leitura da expressão
		Set<IVariableBinding> readVariables = ASTUtil.collectReadableVariables(node);

		// Conjunto de variáveis de escrita da expressão
		Set<IVariableBinding> writeVariables = ASTUtil.collectWritableVariables(node);


		if (!readVariables.isEmpty()) {
			boolean wasAdded = false;
			for (ASTNode node2 : this.readableVariablesByNode.keySet()) {
				if (node.getStartPosition() > node2.getStartPosition()) {
					if (readVariables.contains(this.readableVariablesByNode.get(node2))) {
						this.aux(node, startNode);
						this.slicedStatements.add(statement);
						wasAdded = true;
					}
				}
			}
			if (wasAdded) {
				readVariables.removeAll(this.writableVariables);
				readVariables.removeAll(this.readableVariables);
				this.undiscoveredDeclarations.addAll(readVariables);
			}
		}

		if (!writeVariables.isEmpty()) {
			for (ASTNode node2 : this.writableVariablesByNode.keySet()) {
				if (node.getStartPosition() < node2.getStartPosition()) {
					boolean wasAdded = false;
					for (IVariableBinding variableBinding : writeVariables) {
						if (this.writableVariablesByNode.get(node2).contains(variableBinding)) {
							if (this.writableVariables.contains(variableBinding)){
								this.aux(node, startNode);
							this.slicedStatements.add(statement);
								wasAdded = true;
							}
						}
					}
					if (wasAdded) {
						writeVariables.removeAll(this.writableVariables);
						writeVariables.removeAll(this.readableVariables);
						this.undiscoveredDeclarations.addAll(writeVariables);
					}
				}
			}
		}
	}
	
	private void aux(ASTNode fromNode, ASTNode maxNode) {
		do { 
			if (!(fromNode instanceof Statement)) {
				ASTNode parent = ASTUtil.getParent(fromNode, Statement.class, maxNode);
				if (parent != null && !this.slicedStatements.contains(parent)){
					this.slicedStatements.add((Statement) parent);
				}
			}
		}while((fromNode = fromNode.getParent()) != null && fromNode != maxNode);
	}
	
	@Override
	public void endVisit(ImportDeclaration node) {
		this.imports.add(node.getName().getFullyQualifiedName());
		super.endVisit(node);
	}
	
	@Override
	public void endVisit(ClassInstanceCreation node) { 
		Statement superStatement = (Statement) ASTUtil.getParent(node, Statement.class);
		for (Object o : node.arguments()) {
			decide( (Expression) o, superStatement);
		}
		if (node.getExpression() != null) {
			decide(node.getExpression(), superStatement);
		}
		if (node.getAnonymousClassDeclaration() != null){
			for (Object o : node.getAnonymousClassDeclaration().bodyDeclarations()) { 
				BodyDeclaration bodyDeclaration = (BodyDeclaration) o;
				BlockVisitor blockVisitor = new BlockVisitor();
				if (bodyDeclaration instanceof Initializer) {
					Initializer init = (Initializer) bodyDeclaration;
					init.accept(blockVisitor);
					if (blockVisitor.contains) { 
						this.slicedStatements.add((Statement) ASTUtil.getParent(node, Statement.class)); 
					}
				}else if (bodyDeclaration instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration) bodyDeclaration;
					md.accept(blockVisitor);
					if (blockVisitor.contains) { 
						this.slicedStatements.add(superStatement);
					}
				}else{
					//Do nothing
				}
			}
		}
		super.endVisit(node);
	}
	
	private class BlockVisitor extends ASTVisitor {
		boolean contains = false;
		@Override
		public boolean preVisit2(ASTNode node) {
			if (node instanceof Statement && slicedStatements.contains(node)) {
				contains = true;
			}
			return !contains;
		}
	}

	public HashSet<Statement> getSlicedStatements() {
		return slicedStatements;
	}

	public int getNumberOfDecisionStatements() {
		return numberOfDecisionStatements;
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

	public Set<IVariableBinding> getUndiscoveredDeclarations() {
		return undiscoveredDeclarations;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		// TODO Auto-generated method stub
		return super.preVisit2(node);
	}

	public Set<String> getImports() {
		return imports;
	}

}

