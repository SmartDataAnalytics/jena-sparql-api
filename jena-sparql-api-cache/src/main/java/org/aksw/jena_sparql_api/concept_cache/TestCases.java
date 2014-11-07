package org.aksw.jena_sparql_api.concept_cache;

import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;


class QueryRunner {
    private QueryExecutionFactory sparqlService;

    public QueryRunner(QueryExecutionFactory sparqlService) {
        this.sparqlService = sparqlService;
    }

    public QueryRunner trySelect(String queryString) {
        QueryExecution qe = sparqlService.createQueryExecution(queryString);
        ResultSet rs = qe.execSelect();
        ResultSetFormatter.consume(rs);

        return this;
    }
}

public class TestCases {
    public static void main(String[] args) throws Exception {

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

        QueryExecutionFactory rawService = SparqlServiceBuilder
                .http("http://akswnc3.informatik.uni-leipzig.de:8860/sparql", "http://dbpedia.org")
                .withPagination(100000)
                .create();

        QueryExecutionFactory cachedService = new QueryExecutionFactoryConceptCache(rawService);

        boolean forceCompareFailures = false;
        if(forceCompareFailures) {
            rawService = SparqlServiceBuilder
                    .http("http://linkedgeodata.org/sparql", "http://linkedgeodata.org")
                    .withPagination(100000)
                    .create();
        }

        QueryExecutionFactory sparqlService = new QueryExecutionFactoryCompare(rawService, cachedService);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(); //TestBundleReader.class.getClass().getClassLoader());

        sparqlService = cachedService;
        //sparqlService = rawService;

        QueryRunner runner = new QueryRunner(sparqlService);

        for(int i = 0; i < 1000; ++i) {
            Stopwatch sw = Stopwatch.createStarted();

            String varStr = "?hm_" + i;

            String qs = "Prefix o: <http://dbpedia.org/ontology/> Select Distinct ?s { ?s a o:Airport ; o:city <http://dbpedia.org/resource/Leipzig> }".replace("?s", varStr);
            System.out.println("qs = " + qs);
            runner
//                .trySelect("Prefix o: <http://dbpedia.org/ontology/> Select Distinct ?s { ?s a o:Airport }")
                .trySelect(qs)
                ;

            System.out.println("Time taken: " + sw.elapsed(TimeUnit.MILLISECONDS));
        }



        for(int i = 0; i < 0; ++i) {
            Stopwatch sw = Stopwatch.createStarted();

            Resource r = resolver.getResource("query-lorenz-1a.sparql");
            String queryString = StreamUtils.toString(r.getInputStream());
            Query query = QueryFactory.create(queryString);

            /*
            Op op = Algebra.compile(query);
            op = Algebra.toQuadForm(op);
            System.out.println(op);
            */

            QueryExecution qe = sparqlService.createQueryExecution(query);
            ResultSet rs = qe.execSelect();
            ResultSetFormatter.consume(rs);
            //System.out.println(ResultSetFormatter.asText(rs));

            long elapsed = sw.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Time taken: " + elapsed);
        }

    }
}
