package org.aksw.jena_sparql_api.util.collection;
import java.util.function.Function;

import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Range;

@FunctionalInterface
public interface RangedSupplier<I extends Comparable<I>, O>
	extends Function<Range<I>, ClosableIterator<O>>
{
	default public <X> X unwrap(Class<X> clazz, boolean reflexive) {
    	@SuppressWarnings("unchecked")
		X result = reflexive && this.getClass().isAssignableFrom(clazz)
    		? (X)this
    		: null;

    	return result;
	}

}
