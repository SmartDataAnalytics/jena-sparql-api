package org.aksw.jena_sparql_api.rx.util.collection;
import java.util.function.Function;

import org.aksw.commons.util.delegate.Unwrappable;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

@FunctionalInterface
public interface RangedSupplier<I extends Comparable<I>, O>
    extends Function<Range<I>, Flowable<O>>, Unwrappable
{

}
