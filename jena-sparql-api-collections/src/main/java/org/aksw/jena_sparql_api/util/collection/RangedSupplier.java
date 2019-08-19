package org.aksw.jena_sparql_api.util.collection;
import java.util.function.Function;

import org.aksw.commons.util.contextual.Contextual;

import com.google.common.collect.Range;

import io.reactivex.Flowable;

@FunctionalInterface
public interface RangedSupplier<I extends Comparable<I>, O>
	extends Function<Range<I>, Flowable<O>>, Contextual
{

}
