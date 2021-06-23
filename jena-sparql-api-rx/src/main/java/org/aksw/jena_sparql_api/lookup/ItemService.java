package org.aksw.jena_sparql_api.lookup;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Take a stream of input items and transform it to a stream of output items
 *
 * @author raven
 *
 * @param <I>
 * @param <O>
 */
public interface ItemService<I, O>
    extends Function<Collection<I>, Stream<O>>
{
//    default List<O> toList(Stream<I> in) {
//        List<O> result = apply(in).collect(Collectors.toList());
//        return result;
//    }
}
