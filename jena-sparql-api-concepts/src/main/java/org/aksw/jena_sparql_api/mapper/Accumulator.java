package org.aksw.jena_sparql_api.mapper;

public interface Accumulator<B, T>
{
    public void accumulate(B binding);

    T getValue();
}
