package org.aksw.jena_sparql_api.mapper;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

public class AggJenaWrapper
	implements Aggregator<Binding, NodeValue>
{
	protected org.apache.jena.sparql.expr.aggregate.Aggregator delegate;
	protected FunctionEnv functionEnv;

	public AggJenaWrapper(org.apache.jena.sparql.expr.aggregate.Aggregator delegate) {
		this(delegate, null);
	}

	public AggJenaWrapper(org.apache.jena.sparql.expr.aggregate.Aggregator delegate, FunctionEnv functionEnv) {
		super();
		this.delegate = delegate;
		this.functionEnv = functionEnv;
	}

	@Override
	public Accumulator<Binding, NodeValue> createAccumulator() {
		AccJenaWrapper result = new AccJenaWrapper(delegate.createAccumulator(), functionEnv);
		return result;
	}


}
