package org.aksw.jena_sparql_api.mapper.proxy.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.mapper.proxy.MapperProxyUtils;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class to create Jena bindings for Java functions
 * and to register them at Jena's default FunctionRegistry.
 *
 * @author raven
 *
 */
public class FunctionBinder {
    private static final Logger logger = LoggerFactory.getLogger(FunctionBinder.class);

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
        logger.debug(String.format("Auto-binding SPARQL function %s to %s (invocationTarget: %s)", uri, method, invocationTarget));
        FunctionFactory factory = factory(method, invocationTarget);
        functionRegistry.put(uri, factory);
    }

    public void register(Method method) {
        register(method, null);
    }


    /** Convenience method to register a function at Jena's default registry */
    public void register(Method method, Object invocationTarget) {
        String iri = MapperProxyUtils.deriveIriFromMethod(method, DefaultPrefixes.prefixes);

        if (iri == null) {
            throw new RuntimeException("No @Iri or @IriNs annotation present on method");
        }

        register(iri, method);
    }

    /**
     * Register all static methods with @Iri annotations
     *
     */
    public void registerAll(Class<?> clz) {
        registerAll(clz, null);
    }

    /**
     * Register all methods of the class with the given invocationTarget.
     * If the invocation target is null then all static methods will be registered.
     * Otherwise, if the invocation target is non-null then all non-static methods will be registered.
     *
     * @param clz
     * @param invocationTarget
     */
    public void registerAll(Class<?> clz, Object invocationTarget) {
        for (Method method : clz.getMethods()) {
            String iri = MapperProxyUtils.deriveIriFromMethod(method, DefaultPrefixes.prefixes);

            if (iri != null) {
                boolean isStatic = Modifier.isStatic(method.getModifiers());

                // If the invocation target is null then only register static methods
                // otherwise only register only non-static methods
                if ((invocationTarget == null && isStatic) || (invocationTarget != null && !isStatic)) {
                    register(iri, method, invocationTarget);
                }

            }
        }
    }


    public FunctionFactory factory(Method method) {
        return factory(method, null);
    }

    public FunctionFactory factory(Method method, Object invocationTarget) {
        Function fn = functionGenerator.wrap(method, invocationTarget);
        return iri -> fn;
    }


}
