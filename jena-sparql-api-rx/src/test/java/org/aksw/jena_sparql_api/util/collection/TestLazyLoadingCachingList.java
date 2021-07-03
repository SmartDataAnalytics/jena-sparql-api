package org.aksw.jena_sparql_api.util.collection;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplierLazyLoadingListCache;
import org.aksw.jena_sparql_api.rx.util.collection.RangedSupplierList;
import org.junit.Test;

import com.google.common.collect.Range;

public class TestLazyLoadingCachingList {

	@Test
	public void testWithSparql() throws Exception {
		// FIXME RangedSupplierQuery is currently part of concept_cache; it should go to the rx package!
		
//		Dataset dataset = RDFDataMgr.loadDataset(null);
//		SparqlQueryConnection conn = RDFConnectionFactory.connect(dataset);
//		// SparqlRx.execSelectRaw()
//		
//		Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
//		RangedSupplier<Long, Binding> rs = new RangedSupplierQuery(conn::query, query);
//
//		RangeSupplier<Long, Binding> smartRs = SmartCachingRangedSupplier.wrap(rs);
//		
//		List<Var> resultVars = query.getProjectVars();
//		Flowable<Binding> bindings = rs.apply(Range.open(0l, 1000l));
		
		
	}
	
	
    @Test
    public void test() {
        //fail("Not yet implemented");
        List<String> items = IntStream
                .range(0, 100)
                .mapToObj(i -> "item-" + i)
                .collect(Collectors.toList());

        RangedSupplier<Long, String> tmp = new RangedSupplierList<>(items);

        // Add some delay
        RangedSupplier<Long, String> itemSupplier = (range) -> {
            System.out.println("Supplier: Requested range: " + range);
            try {
                TimeUnit.MILLISECONDS.sleep(50l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return tmp.apply(range);
        };


        RangedSupplier<Long, String> llcl = new RangedSupplierLazyLoadingListCache<String>(
                Executors.newFixedThreadPool(4),
                itemSupplier,
                Range.closedOpen(0l, 17l),
                new RangeCostModel());


        Stream<String> itA = llcl.apply(Range.closedOpen(0l, 10l)).toList().blockingGet().stream();
        Stream<String> itB = llcl.apply(Range.closedOpen(5l, 15l)).toList().blockingGet().stream();
        Stream<String> itC = llcl.apply(Range.openClosed(3l, 13l)).toList().blockingGet().stream();
        Stream<String> itD = llcl.apply(Range.closedOpen(15l, 20l)).toList().blockingGet().stream();
        Stream<String> itE = llcl.apply(Range.closedOpen(15l, 20l)).toList().blockingGet().stream();

        itA.forEach(x -> System.out.println("[A] got item: " + x));
        itB.forEach(x -> System.out.println("[B] got item: " + x));
        itC.forEach(x -> System.out.println("[C] got item: " + x));
        itD.forEach(x -> System.out.println("[D] got item: " + x));
        itE.forEach(x -> System.out.println("[E] got item: " + x));
    }

}
