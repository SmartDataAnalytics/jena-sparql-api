package org.aksw.jena_sparql_api.util.collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Range;

@FunctionalInterface
public interface RangedSupplier<I extends Comparable<I>, O>
	extends Function<Range<I>, Stream<O>>, Contextual
{

}
