package org.aksw.jena_sparql_api.util.collection;

import org.aksw.commons.rx.cache.range.SmartRangeCacheImpl;
import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.junit.Ignore;

@Ignore
public class TestSmartRangeCache
	extends RangeCacheTestSuite
{
	@Override
	protected <T> ListPaginator<T> wrapWithCache(ListPaginator<T> backend) {
		return SmartRangeCacheImpl.wrap(backend);
	}
}
