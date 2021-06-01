package org.aksw.jena_sparql_api.util.collection;

import org.aksw.commons.rx.cache.range.SimpleRangeCache;
import org.aksw.jena_sparql_api.lookup.ListPaginator;

public class TestSimpleRangeCache
	extends RangeCacheTestSuite
{
	@Override
	protected <T> ListPaginator<T> wrapWithCache(ListPaginator<T> backend) {
		return SimpleRangeCache.wrap(backend);
	}
}
