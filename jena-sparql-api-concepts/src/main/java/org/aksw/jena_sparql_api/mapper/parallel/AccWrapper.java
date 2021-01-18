package org.aksw.jena_sparql_api.mapper.parallel;

import org.aksw.jena_sparql_api.mapper.Accumulator;

public interface AccWrapper<I, O, SUBACC>
	extends Accumulator<I, O>
{
	SUBACC getSubAcc();
}
