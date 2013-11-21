/**
 * 
 */
package org.apiminer.analyzer.api;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import org.apiminer.entities.api.ApiClass;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiEnum;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.api.ProjectAnalyserStatistic;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;


/**
 * @author Hudson S. Borges
 */
class APIVisitor extends ASTVisitor {
	
	private Set<ApiClass> apiClasses = new HashSet<ApiClass>();

	private boolean includePublic;
	private boolean includePrivate;
	private boolean includeProtected;
	
	private ProjectAnalyserStatistic statistics = new ProjectAnalyserStatistic();

	public APIVisitor(boolean includePublic, boolean includePrivate,
			boolean includeProtected) {

		super(true);
		
		this.includePublic = includePublic;
		this.includePrivate = includePrivate;
		this.includeProtected = includeProtected;
	}
	
	@Override
	public boolean visit(EnumDeclaration node) {
		this.statistics.increaseNumOfClassDeclaration();
		
		ITypeBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		
		this.statistics.increaseNumOfAcceptedClassDeclaration();
		ApiEnum apiEnum = new ApiEnum(binding.getQualifiedName());
		this.setModifiers(apiEnum, node);
		
		if (apiEnum.isPublic()) {
			if (!this.includePublic){
				return super.visit(node);
			}
		}else if(apiEnum.isPrivate()) {
			if (!this.includePrivate){
				return super.visit(node);
			}
		}else if(apiEnum.isProtected()) {
			if (!this.includeProtected){
				return super.visit(node);
			}
		}else if (!apiEnum.isPublic() && !apiEnum.isPrivate() && !apiEnum.isProtected()) {
			return super.visit(node);
		}
		
		this.apiClasses.add(apiEnum);
		
		for (Object o : node.enumConstants()) {
			EnumConstantDeclaration enumConstantDeclaration = (EnumConstantDeclaration) o;
			apiEnum.getConstants().add(enumConstantDeclaration.getName().getFullyQualifiedName());
		}
		
		for (Object o : node.bodyDeclarations()) {
			if (o instanceof MethodDeclaration) {
				this.methodDeclarationHandler((MethodDeclaration) o, apiEnum);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		this.statistics.increaseNumOfClassDeclaration();
		
		ITypeBinding tb = node.resolveBinding();
		if ( tb == null ) {
			return false;
		}
		
		this.statistics.increaseNumOfAcceptedClassDeclaration();
		ApiClass apiClass = new ApiClass(tb.getQualifiedName());
		apiClass.setInterface(tb.isInterface());
		this.setModifiers(apiClass, node);
		
		if (apiClass.isPublic()) {
			if (!this.includePublic){
				return super.visit(node);
			}
		}else if(apiClass.isPrivate()) {
			if (!this.includePrivate){
				return super.visit(node);
			}
		}else if(apiClass.isProtected()) {
			if (!this.includeProtected){
				return super.visit(node);
			}
		}else if (!apiClass.isPublic() && !apiClass.isPrivate() && !apiClass.isProtected()) {
			return super.visit(node);
		}
		
		this.apiClasses.add(apiClass);
		
		for (MethodDeclaration mdNode : node.getMethods()){
			this.methodDeclarationHandler(mdNode, apiClass);
		}
		
		return true;
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
		
		if (apiMethod.isPublic()) {
			if (!this.includePublic){
				return;
			}
		}else if(apiMethod.isPrivate()) {
			if (!this.includePrivate){
				return;
			}
		}else if(apiMethod.isProtected()) {
			if (!this.includeProtected){
				return;
			}
		}else if (!apiMethod.isPublic() && !apiMethod.isPrivate() && !apiMethod.isProtected()) {
			return;
		}
		
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
		
		if (mb.getExceptionTypes().length > 0){ 
			apiMethod.setThrowsException(true);
		}
		
		apiClass.getApiMethods().add(apiMethod);
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
					if (tagName != null) {
						if (tagName.equalsIgnoreCase("@hide")) {
							apiElement.setHidden(true);
						}else if (tagName.equalsIgnoreCase("@deprecated")){
							apiElement.setDeprecated(true);
						}
					}
					tags.addAll(((TagElement) tag).fragments());
				}
			}
		}else{
			apiElement.setHasJavadoc(false);
			apiElement.setHidden(true);
		}
	}

	public Set<ApiClass> getApiClasses() {
		return this.apiClasses;
	}

	/**
	 * @return the statistics
	 */
	public ProjectAnalyserStatistic getStatistics() {
		return statistics;
	}

}