package org.aksw.jena_sparql_api.mapper.proxy.function;

import org.aksw.commons.util.convert.Converter;

/**
 * Metadata for mapping RDF terms passed as arguments to a SPARQL function
 * to Java objects of the appropriate types so that those Jaa objects can be passed as arguments
 * to the wrapped Java function.
 * 
 * @author raven
 *
 */
public class Param {
	/** The actual class accepted as the parameter */
	protected Class<?> paramClass;
	
	/** The class of inputs for the parameter */
	protected Class<?> inputClass;

	/* Converter from the working class to the actual class */
	protected Converter inputConverter;

	/* The default value of type paramClass */
	protected Object defaultValue;
	
	public Param(Class<?> paramClass, Class<?> inputClass, Converter inputConverter, Object defaultValue) {
		super();
		this.paramClass = paramClass;
		this.inputClass = inputClass;
		this.inputConverter = inputConverter;
		this.defaultValue = defaultValue;
	}

	public Class<?> getParamClass() {
		return paramClass;
	}
	
	public Class<?> getInputClass() {
		return inputClass;
	}
	
	public Converter getInputConverter() {
		return inputConverter;
	}
}
