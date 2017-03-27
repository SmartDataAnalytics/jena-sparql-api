package org.aksw.jena_sparql_api.util.collection;

import com.google.common.collect.Range;

public interface CacheRangeInfo<I extends Comparable<I>> {
	boolean isCached(Range<I> range);
}
