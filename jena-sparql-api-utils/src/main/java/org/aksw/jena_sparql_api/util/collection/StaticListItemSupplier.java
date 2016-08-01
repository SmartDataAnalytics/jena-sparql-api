package org.aksw.jena_sparql_api.util.collection;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.IteratorClosable;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class StaticListItemSupplier<T>
    implements Function<Range<Long>, ClosableIterator<T>>
{
    protected List<T> items;
    
    public StaticListItemSupplier(List<T> items) {
        super();
        this.items = items;
    }

    @Override
    public ClosableIterator<T> apply(Range<Long> range) {
        Range<Long> validRange = Range.closedOpen(0l, (long)items.size());        
        Range<Long> effectiveRange = range.intersection(validRange).canonical(DiscreteDomain.longs());
                
        List<T> subList = items.subList(effectiveRange.lowerEndpoint().intValue(), effectiveRange.upperEndpoint().intValue());
        //ClosableIterator<T>
        Iterator<T> it = subList.iterator();
        IteratorClosable<T> result = new IteratorClosable<>(it, null);
        
        return result;
    }

    @Override
    public String toString() {
        return "StaticListItemSupplier [items=" + items + "]";
    }
}