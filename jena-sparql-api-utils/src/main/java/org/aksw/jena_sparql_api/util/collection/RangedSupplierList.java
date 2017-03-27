package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class RangedSupplierList<T>
    implements RangedSupplier<Long, T>//Function<Range<Long>, ClosableIterator<T>>
{
    protected List<T> items;

    public RangedSupplierList(List<T> items) {
        super();
        this.items = items;
    }

    @Override
    public Stream<T> apply(Range<Long> range) {
        Range<Long> validRange = Range.closedOpen(0l, (long)items.size());
        Range<Long> effectiveRange = range.intersection(validRange).canonical(DiscreteDomain.longs());

        List<T> subList = items.subList(effectiveRange.lowerEndpoint().intValue(), effectiveRange.upperEndpoint().intValue());

        return subList.stream();
    }

    @Override
    public String toString() {
        return "StaticListItemSupplier [items=" + items + "]";
    }

//    public <X> X unwrap(Class<X> clazz, boolean reflexive) {
//    	X result = reflexive && this.getClass().isAssignableFrom(clazz)
//    		? (X)this
//    		: null;
//
//    	return result;
//    }
}