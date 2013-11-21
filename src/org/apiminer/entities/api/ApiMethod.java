package org.apiminer.entities.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apiminer.entities.mining.Transaction;

@SuppressWarnings("serial")
@Entity
public class ApiMethod extends ApiElement {

	public static final String parseFullName(String fullClassName, String methodName, List<String> params){
		StringBuilder sb = new StringBuilder()
			.append(fullClassName)
			.append('.')
			.append(methodName)
			.append('(')
			.append(params.toString().trim().substring(1, params.toString().trim().length()-1))
			.append(')');
		
		return sb.toString();
	}
	
	public static final String parseSimpleFullName(String fullClassName, String methodName, List<String> params){
		StringBuilder sb = new StringBuilder()
			.append(fullClassName)
			.append('.')
			.append(methodName)
			.append('(');
		
		for (int i = 0; i < params.size(); i++) {
			String value = params.get(i);
			int j = 0;
			for (; j < value.length(); j++){
				if (Character.isUpperCase(value.charAt(j))) {
					break;
				}
			}
			if (j < value.length() - 1) {
				sb.append(value.substring(j));
			}else{
				sb.append(value);
			}
			if (i < params.size() - 1) {
				sb.append(", ");
			}
		}
		
		return sb.append(')').toString();
	}

	@ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
	@JoinColumn(name = "api_class_id")
	private ApiClass apiClass;
	
	@Column(name = "is_void")
	private boolean isVoid;
	@Column(name = "is_function")
	private boolean isFunction;
	@Column(name = "is_constructor")
	private boolean isConstructor;

	@Column(name = "is_initializer")
	private boolean isInitializer;

	@Column(name = "return_type", columnDefinition="text")
	private String returnType;
	
	@Column(name = "throws_exception")
	private boolean throwsException;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "ApiMethod_Parameter")
	@OrderColumn(name = "parameter_index")
	private List<String> parametersType = new ArrayList<String>();
	
	@OneToMany(mappedBy = "sourceApiMethod", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
	private List<Transaction> transactions = new ArrayList<Transaction>();
	
	@Column(name = "full_name", columnDefinition="text")
	private String fullName;
	
	@Column(name = "simple_full_name", columnDefinition="text")
	private String simpleFullName;
	
	public ApiMethod() {
		super();
	}

	public ApiMethod(ApiClass apiClass, String name, List<String> paramTypes) {
		this.apiClass = apiClass;
		this.name = name;
		this.parametersType = paramTypes;
		this.transactions = new LinkedList<Transaction>();
		this.fullName = parseFullName(apiClass.getName(), name, paramTypes);
		this.simpleFullName = parseSimpleFullName(apiClass.getName(), name, paramTypes);
	}	

	public ApiClass getApiClass() {
		return apiClass;
	}

	public String getFullName() {
		return fullName;
	}

	public List<String> getParametersType() {
		return parametersType;
	}

	public String getReturnType() {
		return returnType;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public boolean isConstructor() {
		return isConstructor;
	}

	public boolean isFunction() {
		return isFunction;
	}

	public boolean isInitializer() {
		return isInitializer;
	}

	public boolean isVoid() {
		return isVoid;
	}

	public void setApiClass(ApiClass apiClass) {
		this.apiClass = apiClass;
	}

	public void setConstructor(boolean isConstructor) {
		this.isConstructor = isConstructor;
	}

	public void setFunction(boolean isFunction) {
		this.isFunction = isFunction;
	}

	public void setInitializer(boolean isInitializer) {
		this.isInitializer = isInitializer;
	}

	
	public void setParametersType(List<String> parametersType) {
		this.parametersType = parametersType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public void setVoid(boolean isVoid) {
		this.isVoid = isVoid;
	}

	@Override
	public int hashCode() {
		if (id != null) {
			return id.toString().hashCode();
		}else{
			return this.getFullName().hashCode();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof ApiMethod))
			return false;
		ApiMethod other = (ApiMethod) obj;
		//FIXME Temporario, ver se realmente é interessante
		if (this.id == other.id)
			return true;
		if (apiClass == null) {
			if (other.apiClass != null)
				return false;
		} else if (!apiClass.equals(other.apiClass))
			return false;
		if (isConstructor != other.isConstructor)
			return false;
		if (isFunction != other.isFunction)
			return false;
		if (isInitializer != other.isInitializer)
			return false;
		if (isVoid != other.isVoid)
			return false;
		if (parametersType == null) {
			if (other.parametersType != null)
				return false;
		} else if (!parametersType.equals(other.parametersType))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (this.isFunction ? this.returnType : "void") + " " + this.simpleFullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	@PrePersist
	@PreUpdate
	public void updateFullName(){
		this.fullName = parseFullName(apiClass.getName(), name, parametersType);
		this.simpleFullName = parseSimpleFullName(apiClass.getName(), name, parametersType);
	}

	public boolean isThrowsException() {
		return throwsException;
	}

	public void setThrowsException(boolean throwsException) {
		this.throwsException = throwsException;
	}

	public String getSimpleFullName() {
		return simpleFullName;
	}
	
}
