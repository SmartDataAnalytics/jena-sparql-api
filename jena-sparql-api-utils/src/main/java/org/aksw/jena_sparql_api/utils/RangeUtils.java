package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_GreaterThan;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.E_LessThan;
import org.apache.jena.sparql.expr.E_LessThanOrEqual;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.BoundType;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

public class RangeUtils {
    public static final Range<Long> rangeStartingWithZero = Range.atLeast(0l);

    // Note: This method is tied to Jena, whereas all other RangeUtils only depend on Java/Guava.
    // So we should separate these utils
	public static Expr createExpr(Node node, Range<? extends NodeHolder> range) {
		Expr n = ExprUtils.makeNode(node);

		List<Expr> parts = new ArrayList<>();

		if(RangeUtils.isSingleton(range)) {
			parts.add(new E_Equals(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));
		} else {
		
			if(range.hasLowerBound()) {			
				if(range.lowerBoundType().equals(BoundType.OPEN)) {
					parts.add(new E_GreaterThan(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));
				} else {
					parts.add(new E_GreaterThanOrEqual(n, ExprUtils.makeNode(range.lowerEndpoint().getNode())));					
				}
			}
	
			if(range.hasUpperBound()) {			
				if(range.upperBoundType().equals(BoundType.OPEN)) {
					parts.add(new E_LessThan(n, ExprUtils.makeNode(range.upperEndpoint().getNode())));
				} else {
					parts.add(new E_LessThanOrEqual(n, ExprUtils.makeNode(range.upperEndpoint().getNode())));					
				}
			}
		}
		
		Expr result = ExprUtils.andifyBalanced(parts);
		return result;
	}

    
    public static <T extends Comparable<T>> Optional<T> tryGetSingleton(Range<T> range) {
    	boolean isSingleton = isSingleton(range);
    	return Optional.ofNullable(isSingleton ? range.lowerEndpoint() : null);
    }
    
    public static boolean isSingleton(Range<?> range) {
    	boolean result =
    			range.hasLowerBound() &&
    			range.hasUpperBound() &&
    			range.lowerBoundType().equals(BoundType.CLOSED) &&
    			range.upperBoundType().equals(BoundType.CLOSED);
    	
    	return result;
    }

    public static long pickLong(Range<Long> range, Random random) {
    	Range<Long> norm = range.intersection(Range.closed(Long.MIN_VALUE, Long.MAX_VALUE));
    	long l = norm.lowerEndpoint();
    	long u = norm.upperEndpoint();
    	double pick = random.nextDouble();
    	long result = l + Math.round(pick * (u - l));
    	return result;
    }

    public static double pickDouble(Range<Double> range, Random random) {
    	Range<Double> norm = range.intersection(Range.closed(Double.MIN_VALUE, Double.MAX_VALUE));
    	double l = norm.lowerEndpoint();
    	double u = norm.upperEndpoint();
    	double pick = random.nextDouble();
    	double result = l + pick * (u - l);
    	return result;
    }
    
    
    
    public static CountInfo toCountInfo(Range<? extends Number> range) {
    	Long min = range.hasLowerBound() ? range.lowerEndpoint().longValue() : 0;
    	Long max = range.hasUpperBound() ? range.upperEndpoint().longValue() : null;

    	CountInfo result = new CountInfo(
    			min,
    			max == null ? true : !max.equals(min),
    			max
    	);
    	return result;
    }

    /**
     * Convert a range relative within another one to an absolute range
     *
     * @param outer
     * @param relative
     * @param domain
     * @param addition
     * @return
     */
    public static <C extends Comparable<C>> Range<C> makeAbsolute(Range<C> outer, Range<C> relative, DiscreteDomain<C> domain, BiFunction<C, Long, C> addition) {
        long distance = domain.distance(outer.lowerEndpoint(), relative.lowerEndpoint());

        Range<C> shifted = RangeUtils.shift(relative, distance, domain, addition);
        Range<C> result = shifted.intersection(outer);
        return result;
    }


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

    public static Range<Long> multiplyByPageSize(Range<Long> range, long pageSize) {
        Range<Long> result;

        if(range.hasLowerBound()) {
            if(range.hasUpperBound()) {
                result = Range.closedOpen(range.lowerEndpoint() * pageSize, range.upperEndpoint() * pageSize);
            } else {
                result = Range.atLeast(range.lowerEndpoint() * pageSize);
            }
        } else {
            if(range.hasUpperBound()) {
                result = Range.lessThan(range.upperEndpoint() * pageSize);
            } else {
                result = Range.all();
            }

        }

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
