package org.aksw.jena_sparql_api.concept_cache.dirty;

import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewCacheMaster;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;


public class TestCases {
    public static void main(String[] args) throws Exception {

        UpdateRequest x = UpdateFactory.create("Insert { ?s ?p ?o } Where { ?s ?p ?o }");
        UpdateModify y = (UpdateModify)(x.getOperations().get(0));

        Object o= y.toString();
        System.out.println(o);

        if(true) {
            System.exit(0);
        }

        /*
        ApacheLogDirectory dir = new ApacheLogDirectory(new File("/home/raven/Desktop/Datasets/DBpedia"), Pattern.compile("access.log.*"));
        Iterator<ApacheLogEntry> it = dir.getIterator(null, null, false, false);

        while(it.hasNext()) {
            ApacheLogEntry entry = it.next();
            System.out.println(entry.getRequest());
        }

        if(true) {
            System.exit(0);
        }
        */

        QueryExecutionFactory rawService = FluentQueryExecutionFactory
                .http("http://akswnc3.informatik.uni-leipzig.de:8860/sparql", "http://dbpedia.org")
                .config()
                    .withPagination(100000)
                .end()
                .create();

        QueryExecutionFactory cachedService = new QueryExecutionFactoryViewCacheMaster(rawService, null);

        boolean forceCompareFailures = false;
        if(forceCompareFailures) {
            rawService = FluentQueryExecutionFactory
                    .http("http://linkedgeodata.org/sparql", "http://linkedgeodata.org")
                    .config()
                        .withPagination(100000)
                    .end()
                    .create();
        }

        QueryExecutionFactory sparqlService = new QueryExecutionFactoryCompare(rawService, cachedService);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(); //TestBundleReader.class.getClass().getClassLoader());

        sparqlService = cachedService;
        //sparqlService = rawService;

        PrefixMapping prefixMapping = new PrefixMappingImpl();
        prefixMapping.setNsPrefix("r", "http://dbpedia.org/resource/");
        prefixMapping.setNsPrefix("o", "http://dbpedia.org/ontology/");

        QueryRunner runner = new QueryRunner(sparqlService, prefixMapping);

        //for(int i = 0; i < 100; ++i) {
            multiOverlap(runner);
        //}
    }

    public static void multiOverlap(QueryRunner qr) {
        qr
            .trySelect("Select ?s { ?s a o:Airport ; o:city r:Leipzig }")
            .trySelect("Select ?s { ?s a o:Airport ; o:city r:Leipzig }")
            .trySelect("Select ?s { ?s a o:Airport ; o:location r:Germany }")
            .trySelect("Select ?s { ?s a o:Airport ; o:city r:Leipzig ; o:location r:Germany }")
            ;
    }

    public static void stuff(QueryRunner qr) {
//
//        for(int i = 0; i < 1000; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();
//
//            String varStr = "?s_" + i;
//
//            //System.out.println("qs = " + qs);
//            runner
//                //.trySelect("Select Distinct ?s { ?s a o:Airport }")
//                .trySelect("Select Distinct ?s { ?s a o:Airport ; o:city <http://dbpedia.org/resource/Leipzig> }".replace("?s", varStr))
//                //.trySelect("Select Distinct ?s { ?s o:city <http://dbpedia.org/resource/Leipzig> . <http://dbpedia.org/resource/European_Air_Transport> <http://dbpedia.org/ontology/hubAirport> ?s }".replace("?s", varStr))
//                ;
//
//            System.out.println("Time taken: " + sw.elapsed(TimeUnit.MILLISECONDS));
//        }
//
//
//
//        for(int i = 0; i < 0; ++i) {
//            Stopwatch sw = Stopwatch.createStarted();
//
//            Resource r = resolver.getResource("query-lorenz-1a.sparql");
//            String queryString = StreamUtils.toString(r.getInputStream());
//            Query query = QueryFactory.create(queryString);
//
//            /*
//            Op op = Algebra.compile(query);
//            op = Algebra.toQuadForm(op);
//            System.out.println(op);
//            */
//
//            QueryExecution qe = sparqlService.createQueryExecution(query);
//            ResultSet rs = qe.execSelect();
//            ResultSetFormatter.consume(rs);
//            //System.out.println(ResultSetFormatter.asText(rs));
//
//            long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
//            System.out.println("Time taken: " + elapsed);
//        }
    }

}
