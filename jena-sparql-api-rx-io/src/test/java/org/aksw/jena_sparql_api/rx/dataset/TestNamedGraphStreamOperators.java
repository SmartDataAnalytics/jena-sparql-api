package org.aksw.jena_sparql_api.rx.dataset;

import java.util.Iterator;

import org.aksw.commons.io.syscall.sort.SysSort;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.NodesInDatasetImpl;
import org.aksw.jena_sparql_api.rx.DatasetFactoryEx;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;
import org.junit.Test;


public class TestNamedGraphStreamOperators {

//    @Test
//    public void testBlankNodesInQueries() {
//        ARQ.enableBlankNodeResultLabels(true);
//        ARQ.getContext().set(ARQ.constantBNodeLabels, true);
//        ARQ.setNormalMode();
//        Query query = QueryFactory.create("SELECT * { ?s a _:foobar }");
//        System.out.println(query);
//
//    }

//    @Test
//    public void test1() {
//        MainCliNamedGraphStream.mainCore(new String[] {"map", "-o", "nquads", "-s", "CONSTRUCT WHERE { ?s <urn:git:name> ?o ; <urn:git:timestamp> ?t }", "/tmp/stream.trig"});
//    }

    @Test
    public void testInsertOrderRetainingGraph() {
        Dataset ds = RDFDataMgr.loadDataset("ngs-nato-phonetic-alphabet.trig");

        try(QueryExecution qe = QueryExecutionFactory.create("CONSTRUCT { ?s ?p ?o } { GRAPH ?g { ?s <http://xmlns.com/foaf/0.1/name> ?l ; ?p ?o } } ORDER BY ?l ?s ?p ?o", ds)) {


            if(false) {
                Iterator<Triple> it = qe.execConstructTriples();
                while(it.hasNext()) {
                    System.out.println(it.next());
                }
            } else {

                Dataset dsx = DatasetFactoryEx.createInsertOrderPreservingDataset();
                //Dataset dsx = DatasetFactory.wrap(new DatasetGraphQuadsImpl(new QuadTableLinkedHashMap()));
                Model tgt = dsx.getDefaultModel();

                tgt.setNsPrefix("foo", "http://foo.bar/");

                // Model tgt = ModelFactory.createModelForGraph(m); //GraphFactory.createDataBagGraph(ThresholdPolicyFactory.never()));
//                Txn.executeWrite(dsx, () -> qe.execConstruct(tgt));
                qe.execConstruct(tgt);
                tgt.listStatements().mapWith(Statement::asTriple).forEachRemaining(System.out::println);

                System.out.println("Output:");
                RDFDataMgr.write(System.out, dsx, RDFFormat.TRIG_PRETTY);
            }


            //ResulTSetFormqe.execSelect();
        }

    }


//    @Test
//    public void testRaceCondition() {
//        Stream.generate(() -> QueryFactory.create("SELECT * { BIND(SHA256('foo') AS ?bar) }"))
//            .peek(q -> q.setResultVars()) // <-- With this line commented out, the race condition happens earlier
//             // Repeat q to increase chance to cause the race condition
//            .forEach(q -> Arrays.asList(q, q, q, q, q, q, q, q).parallelStream()
//                .forEach(query -> {
//                    Model model = ModelFactory.createDefaultModel();
//                    try(QueryExecution qe = QueryExecutionFactory.create(query, model)) {
//                        ResultSetFormatter.consume(qe.execSelect());
//                    }
//                }));
//
//	}

//	@Test
//	public void testDatasetGraphSize() {
//		Dataset ds = RDFDataMgr.loadDataset("ngs-nato-phonetic-alphabet-single-graph.nq");
//		System.out.println(ds.asDatasetGraph().size());
//
//	}

//	@Test
//	public void testZipWithIndex() {
//		Flowable.fromIterable(() -> IntStream.range(0, 10).mapToObj(x -> UUID.randomUUID()).iterator())
//		.zipWith(LongStream.iterate(0, x -> x + 1)::iterator, SimpleEntry::new)
//		.forEach(System.out::println);
//
//	}

    /**
     * Assert that blank nodes did not get relabeled
     */
    @Test
    public void testBlankNode() {
        Quad q = RDFDataMgrRx.createFlowableQuads("ngs-nato-phonetic-alphabet.trig", Lang.TRIG, null)
        .firstOrError()
        .blockingGet();

        Assert.assertEquals(NodeFactory.createBlankNode("a"), q.getSubject());
    }


//	@Test
    public void testMapToGroup() {


//		Iterator<Quad> it = RDFDataMgr.createIteratorQuads(RDFDataMgr.open("ngs-nato-phonetic-alphabet.trig"), Lang.TRIG, null);
//		while(it.hasNext()) {
//			Quad q = it.next();
//			System.out.println(q.getGraph());
//		}


//		Dataset ds = RDFDataMgr.loadDataset("ngs-nato-phonetic-alphabet.trig");
//		System.out.println(ds.asDatasetGraph().size());
//		RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_PRETTY);

        RDFDataMgrRx.createFlowableDatasets("ngs-nato-phonetic-alphabet.trig", Lang.TRIG, null)
        .map(ResourceInDatasetFlowOps
                .mapToGroupedResourceInDataset(QueryFactory.create("SELECT DISTINCT ?g ?s { GRAPH ?g { ?s ?p ?o } }"))::apply)
        .map(grid -> DatasetFlowOps.serializeForSort(DatasetFlowOps.GSON, grid.getDataset().asDatasetGraph().listGraphNodes().next(), grid))
        .map(line -> DatasetFlowOps.deserializeFromSort(DatasetFlowOps.GSON, line, NodesInDatasetImpl.class))
        .blockingForEach(x -> {
            System.out.println("Grouped " + x);
        });
    }

    @Test
    public void testResourceInDataset() {
        SysSort sortCmd = new SysSort();
        sortCmd.reverse = true;

        RDFDataMgrRx.createFlowableDatasets("ngs-nato-phonetic-alphabet.trig", Lang.TRIG, null)
        .map(ResourceInDatasetFlowOps
                .mapToGroupedResourceInDataset(QueryFactory.create("SELECT DISTINCT ?g ?s { GRAPH ?g { ?s ?p ?o } }"))::apply)
        .compose(ResourceInDatasetFlowOps.createSystemSorter(sortCmd, null))
        .flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset)
        //.compose(DatasetStreamOps.s)
        //.compose(ResourceInDatasetFlowOps.) //FlowableOps.sysCall(SysCalls.createDefaultSortSysCall(sortCmd)))
        .compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
        .flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset)
        .blockingForEach(x -> {
            System.out.println(x);
        })
        ;

        //.compose(MainCliNamedGraphStream.groupedResourceInDataset());


        // .compose(MainCliNamedGraphStream.createS)


        // Main

    }
}
