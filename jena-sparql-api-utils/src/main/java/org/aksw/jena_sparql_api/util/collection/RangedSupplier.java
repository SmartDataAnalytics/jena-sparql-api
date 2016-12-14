package org.aksw.jena_sparql_api.util.collection;
import java.util.function.Function;

import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

@FunctionalInterface
public interface RangedSupplier<I extends Comparable<I>, O>
	extends Function<Range<I>, ClosableIterator<O>>, Contextual
{

}
