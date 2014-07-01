package org.apiminer.extractor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apiminer.entities.api.ApiMethod;
import org.apiminer.util.ASTUtil;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

public class CodeSlicing {
	
	private MethodDeclaration methodDeclaration;
	private Map<ASTNode, ApiMethod> invocations;
	
	private Set<Statement> relatedStatements = new HashSet<Statement>();

	private Set<IVariableBinding> readableVariables = new HashSet<IVariableBinding>();
	private Set<IVariableBinding> writableVariables = new HashSet<IVariableBinding>();
	
	private Map<IVariableBinding, ASTNode> map = new HashMap<IVariableBinding, ASTNode>();
	
	public CodeSlicing(MethodDeclaration node, Map<ASTNode, ApiMethod> invocations) {
		super();
		this.methodDeclaration = node;
		this.invocations = invocations;
		
		for (ASTNode n : invocations.keySet()) {
			// Add the statements of the invocations
			if (!(n instanceof Statement)) {
				this.relatedStatements.add((Statement) ASTUtil.getParent(n, Statement.class));
			} else {
				this.relatedStatements.add((Statement) n);
			}
			
			// Type of node that returns values 
			if (n instanceof Expression) {
				final Set<IVariableBinding> variables = ASTUtil.collectWritableVariables((Expression) n);
				this.readableVariables.addAll(variables);
				for (IVariableBinding variable : variables) {
					this.map.put(variable, n);
				}
			}
			
			if (n instanceof Expression) {
				this.writableVariables.addAll(ASTUtil.collectReadableVariables((Expression) n));
			} else if (n instanceof Statement) {
				if (n instanceof ConstructorInvocation) {
					for (Object e : ((ConstructorInvocation) n).arguments()) {
						this.writableVariables.addAll(ASTUtil.collectReadableVariables((Expression) e));
					}
				} else if (n instanceof SuperConstructorInvocation) {
					for (Object e : ((SuperConstructorInvocation) n).arguments()) {
						this.writableVariables.addAll(ASTUtil.collectReadableVariables((Expression) e));
					}
				}
			}
		}
		
		
		
		
		
	}
	
	private class WritableVariablesVisitor extends ASTVisitor {
		@Override
		public void endVisit(Assignment node) {
			Set<IVariableBinding> variables = ASTUtil.collectVariables2(node.getLeftHandSide());
			for (IVariableBinding v : variables) {
				if (writableVariables.contains(v)) {
					relatedStatements.add((Statement) ASTUtil.getParent(node, Statement.class));
				}
			}
			
		}
	}

	public Block getSlice(){
		
		
		
		return null;
	}

}
