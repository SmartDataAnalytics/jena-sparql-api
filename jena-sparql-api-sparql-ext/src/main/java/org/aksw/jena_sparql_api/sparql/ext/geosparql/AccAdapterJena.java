package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.function.FunctionEnv;

public class AccAdapterJena
	implements Accumulator {

	protected org.aksw.commons.collector.domain.Accumulator<Binding, NodeValue> accDelegate;
	
	public AccAdapterJena(org.aksw.commons.collector.domain.Accumulator<Binding, NodeValue> accDelegate) {
		super();
		this.accDelegate = accDelegate;
	}

	@Override
	public void accumulate(Binding binding, FunctionEnv functionEnv) {
		accDelegate.accumulate(binding);
	}

	@Override
	public NodeValue getValue() {
		NodeValue result = accDelegate.getValue();
		return result;
	}
}