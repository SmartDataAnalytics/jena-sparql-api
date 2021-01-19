package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.Collection;

import org.aksw.jena_sparql_api.mapper.Accumulator;

/** Can be used with AggNatural */
public class AccCollection<I, C extends Collection<I>>
	implements Accumulator<I, C>, Serializable
{
	private static final long serialVersionUID = -377712930606295862L;
	protected C value;
	
	public AccCollection(C value) {
		super();
		this.value = value;
	}
	
	@Override
	public void accumulate(I item) {
		value.add(item);
	}
	
	@Override
	public C getValue() {
		return value;
	}
	
}