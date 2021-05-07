package org.aksw.jena_sparql_api.mapper.proxy.function;

import java.lang.reflect.Method;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 * Convenience class to create Jena bindings for Java functions
 * and to register them at Jena's default FunctionRegistry.
 * 
 * @author raven
 *
 */
public class FunctionBinder {
	protected FunctionGenerator functionGenerator;
	protected FunctionRegistry functionRegistry;
	
	public FunctionBinder() {
		this(new FunctionGenerator(), FunctionRegistry.get());
	}

	public FunctionBinder(FunctionRegistry functionRegistry) {
		this(new FunctionGenerator(), functionRegistry);
	}

	public FunctionBinder(FunctionGenerator functionGenerator) {
		this(functionGenerator, FunctionRegistry.get());
	}

	public FunctionBinder(FunctionGenerator functionGenerator, FunctionRegistry functionRegistry) {
		super();
		this.functionGenerator = functionGenerator;
		this.functionRegistry = functionRegistry;
	}

	public FunctionGenerator getFunctionGenerator() {
		return functionGenerator;
	}
	
	/** Convenience method to register a function at Jena's default registry */
	public void register(String uri, Method method) {
		register(uri, method, null);
	}

	public void register(String uri, Method method, Object invocationTarget) {
		FunctionFactory factory = factory(method, invocationTarget);
		functionRegistry.put(uri, factory);
	}

	public void register(Method method) {
		register(method, null);
	}
	
	/** Convenience method to register a function at Jena's default registry */
	public void register(Method method, Object invocationTarget) {
		Iri iriAnnotation = method.getAnnotation(Iri.class);
		if (iriAnnotation == null) {
			throw new RuntimeException("No @Iri(\"http://my.function\" annotation present on method");
		}
		
		String iri = iriAnnotation.value();
		register(iri, method);
	}

	public FunctionFactory factory(Method method) {
		return factory(method, null);
	}

	public FunctionFactory factory(Method method, Object invocationTarget) {
		Function fn = functionGenerator.wrap(method, invocationTarget);
		return iri -> fn;
	}


}
