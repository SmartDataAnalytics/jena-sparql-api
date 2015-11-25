package org.aksw.jena_sparql_api.mapper.context;

import org.springframework.expression.spel.ast.ValueRef;

public interface Value {
	boolean isPrimitive();
	boolean isRef();

	ValueRef asRef();
}
