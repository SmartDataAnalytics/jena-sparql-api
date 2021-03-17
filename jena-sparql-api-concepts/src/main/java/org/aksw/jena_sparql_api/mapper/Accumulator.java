package org.aksw.jena_sparql_api.mapper;

public interface Accumulator<B, T>
{
    void accumulate(B binding);

    T getValue();
}
