package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.lookup.ListPaginatorFromList;
import org.junit.Test;

import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;

public abstract class RangeCacheTestSuite {
	
	protected abstract <T> ListPaginator<T> wrapWithCache(ListPaginator<T> backend);
	
	@Test
	public void test() {

		List<String> items = LongStream.range(0, 10000)
				.mapToObj(i -> "item #" + i)
				.collect(Collectors.toList());
		
		ListPaginator<String> backend = ListPaginatorFromList.wrap(items);
		
		ListPaginator<String> frontend = wrapWithCache(backend);
		
		Flowable<String> flow = frontend.apply(Range.closedOpen(10l, 20l));
		
		System.out.println(flow.toList().blockingGet());
	}

}
