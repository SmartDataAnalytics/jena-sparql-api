package org.aksw.jena_sparql_api.sparql.ext.benchmark;

import java.util.List;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase0;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class E_NextLong
    extends FunctionBase0
{
	public static final Symbol symNextLong = Symbol.create("http://jsa.aksw.org/nextLong");
	
	@Override
	protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
		Context cxt = env.getContext();
		if(cxt == null) {
			throw new RuntimeException("Cannot use nextId function without a context");			
		}
		
		Long value = cxt.get(symNextLong);
		if(value == null) {
			value = 0l;
		}
		
		cxt.set(symNextLong, value + 1);

		return NodeValue.makeInteger(value);
	}

	@Override
	public NodeValue exec() {
		throw new IllegalAccessError("Method cannot not be called directly");
	}
}
