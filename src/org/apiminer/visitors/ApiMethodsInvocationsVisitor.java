package org.apiminer.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.entities.api.ApiMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class ApiMethodsInvocationsVisitor extends ASTVisitor {
	
	Map<ASTNode, ApiMethod> map = new HashMap<ASTNode, ApiMethod>();

	@Override
	public void endVisit(ClassInstanceCreation node) {
		IMethodBinding binding = node.resolveConstructorBinding();
		if (binding != null) {
			this.invocationHandler(binding, node);
		}
		super.endVisit(node);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		IMethodBinding binding = node.resolveConstructorBinding();
		if (binding != null) {
			this.invocationHandler(binding, node);
		}
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null) {
			this.invocationHandler(binding, node);
		}
		super.endVisit(node);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		IMethodBinding binding = node.resolveConstructorBinding();
		if (binding != null) {
			this.invocationHandler(binding, node);
		}
		super.endVisit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		IMethodBinding binding = node.resolveMethodBinding();
		if (binding != null) {
			this.invocationHandler(binding, node);
		}
		super.endVisit(node);
	}
	
	private void invocationHandler(IMethodBinding methodBinding, ASTNode invocation){
		// Obtenho informações da invocação, como nome do metodo, classe e argumentos
		String methodName = methodBinding.getName();
		String className = methodBinding.getDeclaringClass().getQualifiedName();
		List<String> params = new ArrayList<String>(methodBinding.getParameterTypes().length);
		for (int i = 0; i < methodBinding.getParameterTypes().length; i++) {
			params.add(i,methodBinding.getParameterTypes()[i].getQualifiedName());
		}

		// Busca na base por informações do método
		ApiMethod apiMethod = new ApiMethodDAO().find(1, ApiMethod.parseFullName(className, methodName, params), DatabaseType.PRE_PROCESSING);

		// Se o método esta presente na API analisada, esta invocação é incluída
		if (apiMethod != null) {
			this.map.put(invocation, apiMethod);
		}
	}
	

}
