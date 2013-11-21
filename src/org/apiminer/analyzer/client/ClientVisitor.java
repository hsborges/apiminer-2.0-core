/**
 * 
 */
package org.apiminer.analyzer.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiEnum;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.Project;
import org.apiminer.entities.api.ProjectAnalyserStatistic;
import org.apiminer.entities.mining.Transaction;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author Hudson S. Borges
 */
class ClientVisitor extends ASTVisitor {

	private ProjectAnalyserStatistic statistics = new ProjectAnalyserStatistic();
	private Set<ApiClass> apiClasses = new HashSet<ApiClass>();
	private Project sourceProject = null;
	private ApiMethodDAO methodDAO = new ApiMethodDAO();
	
	public ClientVisitor(Project sourceProject) {
		super(true);
		if (sourceProject == null) {
			throw new IllegalArgumentException("Source must be not null");
		}else{
			this.sourceProject = sourceProject;
		}
	}

	private ApiClass classHandler(ITypeBinding declaredClass) {
		ApiClass apc = new ApiClass(declaredClass.getQualifiedName());
		setModifiers(apc, declaredClass);
		apc.setInterface(declaredClass.isInterface());
		return apc;
	}
	
	private ApiEnum enumHandler(ITypeBinding declaredEnum) {
		ApiEnum ape = new ApiEnum(declaredEnum.getQualifiedName());
		setModifiers(ape, declaredEnum);
		ape.setInterface(declaredEnum.isInterface());
		return ape;
	}

	public Set<ApiClass> getApiClasses() {
		return apiClasses;
	}

	/**
	 * @return the statistics
	 */
	public ProjectAnalyserStatistic getStatistics() {
		return statistics;
	}

	private void setModifiers(ApiElement m, IBinding b) {
		m.setAbstract(Modifier.isAbstract(b.getModifiers()));
		m.setFinal(Modifier.isFinal(b.getModifiers()));
		m.setPrivate(Modifier.isPrivate(b.getModifiers()));
		m.setProtected(Modifier.isProtected(b.getModifiers()));
		m.setPublic(Modifier.isPublic(b.getModifiers()));
		m.setDefault(!m.isPrivate() && !m.isPublic() && !m.isProtected());
		m.setStatic(Modifier.isStatic(b.getModifiers()));
	}
	
	@Override
	public boolean visit(EnumDeclaration node) {
		
		if (node.isPackageMemberTypeDeclaration() || node.isMemberTypeDeclaration() ) {
		
			statistics.increaseNumOfClassDeclaration();
			ITypeBinding typeBinding = node.resolveBinding();
			if (typeBinding == null) {
				return super.visit(node);
			}
			
			statistics.increaseNumOfAcceptedClassDeclaration();
			ApiEnum ape = this.enumHandler(typeBinding);
			for (Object o : node.enumConstants()) {
				EnumConstantDeclaration ecd = (EnumConstantDeclaration) o;
				ape.getConstants().add(ecd.getName().toString());
			}
			
			for (Object o : node.bodyDeclarations()) {
				BodyDeclaration type = (BodyDeclaration) o;
				if (type instanceof MethodDeclaration) {
					IMethodBinding mb = ((MethodDeclaration) type).resolveBinding();
					
					if (mb == null) {
						continue;
					}else{
					
						statistics.increaseNumOfMethodDeclaration();
						LinkedList<String> parameters = new LinkedList<String>();
						for (ITypeBinding pb : mb.getParameterTypes()) {
							parameters.add(pb.getQualifiedName());
						}
						ApiMethod apiMethod = new ApiMethod(ape, mb.getName(), parameters);
						
						this.setModifiers(apiMethod, mb);
						apiMethod.setConstructor(mb.isConstructor());
						
						String returnType = mb.getReturnType().getQualifiedName();
						if (returnType.trim().equalsIgnoreCase("void")) {
							apiMethod.setVoid(true);
							apiMethod.setFunction(false);
							apiMethod.setReturnType(null);
						}else{
							apiMethod.setVoid(false);
							apiMethod.setFunction(true);
							apiMethod.setReturnType(returnType);
						}	
						
						InvocationVisitor invocationVisitor = new InvocationVisitor();
						type.accept(invocationVisitor);
						
						apiMethod.getTransactions().add(new Transaction(apiMethod, new HashSet<ApiMethod>(invocationVisitor.apiMethods)));
						
						ape.getApiMethods().add(apiMethod);
						
						statistics.increaseNumOfAcceptedMethodDeclaration();
						
					}
				}
			}
			
			this.apiClasses.add(ape);
		
		}
		
		return super.visit(node);
		
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		
		if (node.isPackageMemberTypeDeclaration() || node.isMemberTypeDeclaration() ) {
		
			statistics.increaseNumOfClassDeclaration();
			ITypeBinding typeBinding = node.resolveBinding();
			if (typeBinding == null) {
				return super.visit(node);
			}
			
			statistics.increaseNumOfAcceptedClassDeclaration();
			ApiClass apc = this.classHandler(typeBinding);
			
			for (Object o : node.bodyDeclarations()) {
				BodyDeclaration type = (BodyDeclaration) o;
				if (type instanceof MethodDeclaration) {
					IMethodBinding mb = ((MethodDeclaration) type).resolveBinding();
					
					if (mb == null) {
						continue;
					}else{
					
						statistics.increaseNumOfMethodDeclaration();
						LinkedList<String> parameters = new LinkedList<String>();
						for (ITypeBinding pb : mb.getParameterTypes()) {
							parameters.add(pb.getQualifiedName());
						}
						ApiMethod apiMethod = new ApiMethod(apc, mb.getName(), parameters);
						
						this.setModifiers(apiMethod, mb);
						apiMethod.setConstructor(mb.isConstructor());
						
						String returnType = mb.getReturnType().getQualifiedName();
						if (returnType.trim().equalsIgnoreCase("void")) {
							apiMethod.setVoid(true);
							apiMethod.setFunction(false);
							apiMethod.setReturnType(null);
						}else{
							apiMethod.setVoid(false);
							apiMethod.setFunction(true);
							apiMethod.setReturnType(returnType);
						}	
						
						InvocationVisitor invocationVisitor = new InvocationVisitor();
						type.accept(invocationVisitor);
						
						apiMethod.getTransactions().add(new Transaction(apiMethod, new HashSet<ApiMethod>(invocationVisitor.apiMethods)));
						
						apc.getApiMethods().add(apiMethod);
						
						statistics.increaseNumOfAcceptedMethodDeclaration();
						
					}
				}
			}
			
			this.apiClasses.add(apc);
		
		}
		
		return super.visit(node);
		
	}
	
	private class InvocationVisitor extends ASTVisitor {
		
		private List<ApiMethod> apiMethods = new ArrayList<ApiMethod>();
		
		@Override
		public boolean visit(ClassInstanceCreation node) {
			this.resolveMethodInvocation(node, node.resolveConstructorBinding());
			return super.visit(node);
		}
		
		@Override
		public boolean visit(ConstructorInvocation node) {
			this.resolveMethodInvocation(node, node.resolveConstructorBinding());
			return super.visit(node);
		}
		
		@Override
		public boolean visit(MethodInvocation node) {
			this.resolveMethodInvocation(node, node.resolveMethodBinding());
			return super.visit(node);
		}
		

		@Override
		public boolean visit(SuperMethodInvocation node) {
			this.resolveMethodInvocation(node, node.resolveMethodBinding());
			return super.visit(node);
		}
		
		private void resolveMethodInvocation(ASTNode node, IMethodBinding methodBinding){
			
			statistics.increaseNumOfMethodInvocations();
			if (methodBinding == null) {
				return;
			}
			
			// Obtendo informacoes do metodo fonte que a chamada esta fazendo
			LinkedList<String> parameters = new LinkedList<String>();
			for (ITypeBinding pb : methodBinding.getParameterTypes()) {
				parameters.add(pb.getQualifiedName());
			}
			
			String methodCalledName = ApiMethod.parseFullName(methodBinding.getDeclaringClass().getQualifiedName(), methodBinding.getName(), parameters);
			ApiMethod calledMethod = methodDAO.find(sourceProject.getId(), methodCalledName, DatabaseType.PRE_PROCESSING);
			
			// Se nao for um metodo da API analisada, continua analisando outros
			if (calledMethod == null) {
				return;
			}
			
			// Adiciona a invocação ao metodo
			this.apiMethods.add(calledMethod);
			
			statistics.increaseNumOfAcceptedMethodInvocations();
			
		}
		
	}

}