package org.aksw.jena_sparql_api.mapper;

/**
 *
 * @author raven
 *
 * Note: We could make this a Supplier<Accumulator<B, T>>
 *
 * @param <B> The type of bindings being accumulated by the accumulator
 * @param <T> The result object of the accumulation
 */
@FunctionalInterface
public interface Aggregator<B, T> {
    Accumulator<B, T> createAccumulator();
}