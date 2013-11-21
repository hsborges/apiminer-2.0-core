package org.apiminer.builder;


/**
 * 
 * @author Hudson S. Borges
 *
 */
public interface IBuilder {

	boolean build(String path) throws BuilderException;
	
	String getBuilderName();

}
