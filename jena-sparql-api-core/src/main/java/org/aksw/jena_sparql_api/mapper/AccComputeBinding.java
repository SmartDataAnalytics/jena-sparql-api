package org.aksw.jena_sparql_api.mapper;

import java.util.Map;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

public class AccComputeBinding<B, T>
	implements Accumulator<B, T>
{
	//protected org.apache.jena.sparql.expr.aggregate.Accumulator delegate;
	protected Map<Var, Function<B, Node>> varToNodeFn;
	protected Accumulator<Binding, T> delegate;

	public AccComputeBinding(Map<Var, Function<B, Node>> varToNodeFn, Accumulator<Binding, T> delegate) {
		super();
		this.varToNodeFn = varToNodeFn;
		this.delegate = delegate;
	}

	@Override
	public void accumulate(B input) {
		BindingHashMap binding = new BindingHashMap();

		varToNodeFn.entrySet().forEach(e -> {
			Node node = e.getValue().apply(input);
			binding.add(e.getKey(), node);
		});

		delegate.accumulate(binding);
	}

	@Override
	public T getValue() {
		T result = delegate.getValue();
		return result;
	}

}
