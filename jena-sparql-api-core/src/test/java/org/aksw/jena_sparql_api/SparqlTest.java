package org.aksw.jena_sparql_api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.concurrent.CountingCompletionService;
import org.aksw.jena_sparql_api.compare.QueryExecutionCompare;
import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class QueryRunnable
    implements Runnable
{
    private static final Logger logger = LoggerFactory
            .getLogger(QueryRunnable.class);

    private int nLoops;
    private int nResources;
    private Random rand;
    private QueryExecutionFactoryCompare qef;

    public QueryRunnable(int nLoops, Random rand, int nResources, QueryExecutionFactoryCompare qef) {
        this.nLoops = nLoops;
        this.rand = rand;
        this.nResources = nResources;
        this.qef = qef;
    }

    @Override
    public void run() {
        logger.debug("Starting query runner");
        for(int i = 0; i < nLoops; ++i) {
            int id = rand.nextInt(nResources);
            String queryStr = SparqlTest.createTestQueryString(id);
            QueryExecutionCompare qe = qef.createQueryExecution(queryStr);
            ResultSet rs = qe.execSelect();
            if(qe.isDifference()) {
                throw new RuntimeException("Dammit - difference in output");
            }
            ResultSetFormatter.consume(rs);
        }
        logger.debug("Stopping query runner");
    }
}

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/27/11
 *         Time: 12:27 AM
 */
public class SparqlTest {

    static { JenaSystem.init(); }


//    @BeforeClass
//    public static void setUp() {
//        PropertyConfigurator.configure("log4j.properties");
//    }

    public static final String prefix = "http://example.org/resource/item";

    public static Model createTestModel(int n) {
        Model result = ModelFactory.createDefaultModel();

        for(int i = 0; i < n; ++i) {
            Resource s = result.createResource(prefix + i);
            result.add(s, RDF.type, OWL.Thing);
        }

        return result;
    }


    public static String createTestQueryString(int i) {
        String result = "SELECT * { <" + prefix + i + "> ?p ?o }"; //a <http://www.w3.org/2002/07/owl#Thing> }";
        return result;
    }



    @Test
    public void testMultiThreaded() throws InterruptedException, ClassNotFoundException, SQLException, IOException {
        int nThreads = 4;
        int nResources = 50;
        int nLoops = 100;


        Model model = createTestModel(nResources);
        QueryExecutionFactory qefBase = new QueryExecutionFactoryModel(model);

        QueryExecutionFactory qef = qefBase;

//        QueryExecutionFactory qef2 = new QueryExecutionFactoryModel(model);
//        QueryExecutionFactory qef3 = new QueryExecutionFactoryModel(model);


        //qef = new QueryExecutionFactoryRetry(qef, 5, 1);

        // Add delay in order to be nice to the remote server (delay in milli seconds)
        //qef = new QueryExecutionFactoryDelay(qef, 1);

        // Set up a cache
        // Cache entries are valid for 1 day
        long timeToLive = 24l * 60l * 60l * 1000l;

        // This creates a 'cache' folder, with a database file named 'sparql.db'
        // Technical note: the cacheBackend's purpose is to only deal with streams,
        // whereas the frontend interfaces with higher level classes - i.e. ResultSet and Model


        //CacheBackendDao dao = new CacheBackendDaoPostgres();
        //CacheBackend cacheBackend = new CacheBackendDataSource(dataSource, dao);
        //CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);
        //qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);

//
//        // Add pagination
        qef = new QueryExecutionFactoryPaginated(qef, 900);


        QueryExecutionFactoryCompare qefCompare = new QueryExecutionFactoryCompare(qef, qefBase);



        ExecutorService es = Executors.newFixedThreadPool(nThreads);
        CountingCompletionService<?> cs = new CountingCompletionService<Void>(es);

        Random rand = new Random();

        for(int i = 0; i < nThreads; ++i) {
            Runnable runnable = new QueryRunnable(nLoops, rand, nResources, qefCompare);
            //runnable.run();
            cs.submit(runnable, null);
        }

        boolean failed = false;
        while(cs.hasUncompletedTasks()) {
            Future<?> f = cs.take();
            try {
                f.get();
            } catch(Exception e) {
                e.printStackTrace();
                failed = true;
            }
        }

        Thread.sleep(10000);

        es.shutdown();
        es.awaitTermination(20, TimeUnit.SECONDS);

        if(failed) {
            throw new RuntimeException("Test failed due to exceptions in threads");
        }
    }


    public QueryExecutionFactory createService() {
        String service = "http://dbpedia.org/sparql";
        List<String> defaultGraphNames = Arrays.asList("http://dbpedia.org");
        QueryExecutionFactory f = new QueryExecutionFactoryHttp(service, defaultGraphNames);

        return f;
    }

    //@Test
    public void testHttp() {
        String service = "http://dbpedia.org/sparql";
        List<String> defaultGraphNames = Arrays.asList("http://dbpedia.org");
        QueryExecutionFactory f = new QueryExecutionFactoryHttp(service, defaultGraphNames);

        assertEquals("http://dbpedia.org", f.getState());
        assertEquals("http://dbpedia.org/sparql", f.getId());

        QueryExecution qe = f.createQueryExecution("Select * {?s ?p ?o .} limit 3");
        ResultSet rs = qe.execSelect();
        System.out.println(ResultSetFormatter.asText(rs));
    }

    //@Test
    public void testHttpDelay() {
        QueryExecutionFactory f = createService();

        long delay = 5000;
        f = new QueryExecutionFactoryDelay(f, delay);

        long start = System.currentTimeMillis();

        ResultSetFormatter.consume(f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect());
        ResultSetFormatter.consume(f.createQueryExecution("Select * {?s ?p ?o .} limit 3").execSelect());

        long elapsed = System.currentTimeMillis() - start;

        assertTrue(elapsed > 0.9f * delay);

    }

    //@Test
    public void testPagination() {
        System.out.println("Starting testPagination");

        Model model = ModelFactory.createDefaultModel();
        model.add(RDF.type, RDF.type, RDF.type);
        model.add(RDF.List, RDF.type, RDF.List);

        QueryExecutionFactory f = new QueryExecutionFactoryModel(model);


        //QueryExecutionFactory f = createService();
        f = new QueryExecutionFactoryDelay(f, 5000);


        f = new QueryExecutionFactoryPaginated(f, 1);

        QueryExecution q = f.createQueryExecution("Select * {?s ?p ?o}");
        ResultSet rs = q.execSelect();
        while(rs.hasNext()) {
            System.out.println("Here");
            System.out.println(rs.next());
        }

    }


    @Test
    public void testPaginationSelectConstruct() {

        System.out.println("Starting testPagination");

        Model model = ModelFactory.createDefaultModel();
        model.add(RDF.type, RDF.type, RDF.type);
        model.add(RDF.List, RDF.type, RDF.List);
        model.add(RDF.Seq, RDF.type, RDF.Seq);

        QueryExecutionFactory f = new QueryExecutionFactoryModel(model);


        //QueryExecutionFactory f = createService();
        //f = new QueryExecutionFactoryDelay(f, 5000);


        f = new QueryExecutionFactoryPaginated(f, 1);

        String queryString = "Construct { ?s a ?o } { ?s a ?o }";

        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

        QueryExecution q = f.createQueryExecution(queryString);
        Model result = q.execConstruct();

        model.write(System.out, "N-TRIPLES");
        System.out.println("Blah");
        result.write(System.out, "N-TRIPLES");
        //assertEquals(model, result);
    }

    //@Test
    public void testPaginationSelectComplex() {
        System.out.println("Starting testPagination");

        Model model = ModelFactory.createDefaultModel();
        model.add(RDF.type, RDF.type, RDF.type);
        model.add(RDF.List, RDF.type, RDF.List);

        QueryExecutionFactory f = new QueryExecutionFactoryModel(model);


        //QueryExecutionFactory f = createService();
        //f = new QueryExecutionFactoryDelay(f, 5000);


        f = new QueryExecutionFactoryPaginated(f, 1);

        String queryString = "SELECT ?p (COUNT(?s) AS ?count) WHERE {?s ?p ?o. {SELECT ?s ?o WHERE {?s a ?o.} } }";

        Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

        QueryExecution q = f.createQueryExecution(queryString);
        ResultSet rs = q.execSelect();
        while(rs.hasNext()) {
            System.out.println("Here");
            System.out.println(rs.next());
        }




        /*
        String query = String.format(queryTemplate, propertyToDescribe, limit, offset);
Map<ObjectProperty, Integer> result = new HashMap<ObjectProperty, Integer>();
ObjectProperty prop;
Integer oldCnt;
boolean repeat = true;
QueryExecutionFactory f = new QueryExecutionFactoryHttp(ks.getEndpoint().getURL().toString(), ks.getEndpoint().getDefaultGraphURIs());
f = new QueryExecutionFactoryPaginated(f, limit);
QueryExecution exec = f.createQueryExecution(QueryFactory.create(query, Syntax.syntaxARQ));
ResultSet rs = exec.execSelect();
int i = 0;
QuerySolution qs;
while(rs.hasNext() && ++i <= maxFetchedRows){
qs = rs.next();
prop = new ObjectProperty(qs.getResource("p").getURI());
int newCnt = qs.getLiteral("count").getInt();
oldCnt = result.get(prop);
if(oldCnt == null){
oldCnt = Integer.valueOf(newCnt);
}
result.put(prop, oldCnt);
qs.getLiteral("count").getInt();
}
*/
    }


}



class QueryThread
    extends Thread
{
    private String queryString;
    private QueryExecutionFactory factory;
    private boolean queryType;

    private boolean isCancelled = false;

    public QueryThread(QueryExecutionFactory factory, String queryString, boolean queryType) {
        this.factory = factory;
        this.queryString = queryString;
        this.queryType = queryType;
    }

    @Override
    public void interrupt() {
        this.isCancelled = true;
        super.interrupt();
    }

    @Override
    public void run() {
        while(!isCancelled) {
            QueryExecution qe = factory.createQueryExecution(queryString);

            if(queryType == true) {
                ResultSet rs = qe.execSelect();
                ResultSetFormatter.consume(rs);
            } else {
                Model m = qe.execConstruct();
            }
        }
    }
}