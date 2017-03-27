package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

public class AccJenaWrapper
	implements Acc<NodeValue>
{
	protected org.apache.jena.sparql.expr.aggregate.Accumulator delegate;
	protected FunctionEnv functionEnv;

    public AccJenaWrapper(Accumulator delegate) {
    	this(delegate, null);
    }

    public AccJenaWrapper(Accumulator delegate, FunctionEnv functionEnv) {
		super();
		this.delegate = delegate;
		this.functionEnv = functionEnv;
	}

    @Override
	public void accumulate(Binding binding) {
    	delegate.accumulate(binding, functionEnv);
    }

    @Override
    public NodeValue getValue() {
    	return delegate.getValue();
    }

}
