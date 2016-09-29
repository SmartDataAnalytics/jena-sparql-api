package org.aksw.jena_sparql_api.utils;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class RangeUtils {
    public static final Range<Long> rangeStartingWithZero = Range.atLeast(0l);


    public static <C extends Comparable<C>> Range<C> shift(Range<C> range, long distance, DiscreteDomain<C> domain) {
    	BiFunction<C, Long, C> addition = (item, d) -> {
    		C result = item;
    		if(d >= 0) {
    			for(int i = 0; i < d; ++i) {
    				result = domain.next(item);
    			}
    		} else {
    			for(int i = 0; i < -d; ++i) {
    				result = domain.previous(item);
    			}
    		}
    		return result;
    	};

    	Range<C> result = shift(range, distance, domain, addition);
    	return result;
    }

    public static <C extends Comparable<C>> Range<C> shift(Range<C> rawRange, long distance, DiscreteDomain<C> domain, BiFunction<C, Long, C> addition) {

    	Range<C> range = rawRange.canonical(domain);

		Range<C> result;
		if(range.hasLowerBound()) {
			C oldLower = range.lowerEndpoint();
			C newLower = addition.apply(oldLower, distance);

			if(range.hasUpperBound()) {
				C oldUpper = range.upperEndpoint();
				C newUpper = addition.apply(oldUpper, distance);
				result = Range.closedOpen(newLower, newUpper);
			} else {
				result = Range.atLeast(oldLower);
			}

		} else {
			throw new IllegalArgumentException("Cannot displace a range without lower bound");
		}

		return result;
    }

    public static <K extends Comparable<K>, V> Set<Entry<Range<K>, V>> getIntersectingRanges(Range<K> r, Collection<Entry<Range<K>, V>> ranges) {
        Set<Entry<Range<K>, V>> result = ranges.stream()
            .filter(e -> !r.intersection(e.getKey()).isEmpty())
            .collect(Collectors.toSet());

        return result;
    }

    //public static NavigableMap<T extends Comparable> getOverlapping items

    public static Range<Long> startFromZero(Range<Long> range) {
        Range<Long> result = range.intersection(rangeStartingWithZero);
        return result;
    }


    public static PageInfo<Long> computeRange(Range<Long> range, long pageSize) {
        // Example: If pageSize=100 and offset = 130, then we will adjust the offset to 100, and use a subOffset of 30
        long o = range.hasLowerBound() ? range.lowerEndpoint() : 0;

        long subOffset = o % pageSize;
        o -= subOffset;

        // Adjust the limit to a page boundary; the original limit becomes the subLimit
        // And we will extend the new limit to the page boundary again.
        // Example: If pageSize=100 and limit = 130, then we adjust the new limit to 200

        Range<Long> outerRange;
        Range<Long> innerRange;
        if(range.hasUpperBound()) {
            long limit = range.upperEndpoint() - range.lowerEndpoint();
            long l = limit;


            long mod = l % pageSize;
            long extra = mod != 0 ? pageSize - mod : 0;
            l += extra;

            outerRange = Range.closedOpen(o, o + l);
            innerRange = Range.closedOpen(subOffset, limit);
        } else {
            outerRange = Range.atLeast(o);
            innerRange = Range.atLeast(subOffset);
        }

        PageInfo<Long> result = new PageInfo<>(outerRange, innerRange);

        return result;
    }
}
