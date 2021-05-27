package org.aksw.jena_sparql_api.rx.util.collection;

import org.aksw.commons.collections.cache.Cache;

import com.google.common.collect.Range;

/**
 * RangeMap's .subRangeMap method may modify the first and last range due to
 * intersection with the requested range. Hence, we need to keep a copy
 * of the original range object
 * @author raven
 *
 * @param <T>
 */
public class CacheRangeEntry<T> {
    Range<Long> range;
    Cache<T> cache;

    public CacheRangeEntry(Range<Long> range, Cache<T> cache) {
        super();
        this.range = range;
        this.cache = cache;
    }
}