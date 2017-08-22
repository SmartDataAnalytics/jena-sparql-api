package org.aksw.jena_sparql_api.concept_cache.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aksw.combinatorics.collections.CombinatoricsVector;
import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.compare.QueryExecutionFactoryCompare;
import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.OpExecutorFactoryViewMatcher;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryViewCacheMaster;
import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewMatcherQfpc;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.utils.transform.F_QueryTransformDatasetDescription;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.base.Stopwatch;

public class MainTestSparqlViewCache {

    private static final Logger logger = LoggerFactory.getLogger(MainTestSparqlViewCache.class);


//    public void index(Query query) {
//
//        String varName = "a";
//        Var v = Var.alloc(varName);
//        List<String> varList = Arrays.asList(varName);
//        List<Binding> bindings = new ArrayList<Binding>();
//
//        BindingHashMap binding = new BindingHashMap();
//        binding.add(v, RDF.type.asNode());
//
//        bindings.add(binding);
//
//        QueryIterator queryIter = new QueryIterPlainWrapper(bindings.iterator());
//
//        ResultSet rs = ResultSetFactory.create(queryIter, varList);
//        rs = ResultSetFactory.copyResults(rs);
//
//
//
//        //VarUtils.
//
//        //Set<Var> v
//
//        conceptMap.index(query, rs);
//    }

    public ResultSet execSelect(Query query) {

        //conceptMap.lookup(query);
        //RestrictionManagerI


        //OpQopFilter.getSubOp();

        //System.out.println(op);
        //System.out.println(quads);
        //System.out.println(rm);

        return null;
    }



    public static void main(String[] args) throws IOException {
        String data = "simple";



        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("data-" + data + ".nt");



        String fileName = resource.getFilename();
        System.out.println("Trying to load data from " + fileName);

        Dataset model = RDFDataMgr.loadDataset(fileName);
        QueryExecutionFactory rawQef = FluentQueryExecutionFactory
            .from(model)
            //.http("http://akswnc3.informatik.uni-leipzig.de/data/dbpedia/sparql", "http://dbpedia.org")
            //.http("http://localhost:8890/sparql", "http://dbpedia.org")
            .config()
                .withParser(SparqlQueryParserImpl.create(Syntax.syntaxARQ))
                .withQueryTransform(F_QueryTransformDatasetDescription.fn)
                .withPagination(100000)
            .end()
            .create();



//        System.out.println(ResultSetFormatter.asText(rawQef.createQueryExecution(
//          "SELECT * { GRAPH ?g {  ?s <http://ex.org/p1> ?o1 ; <http://ex.org/p2> ?o2 } }").execSelect()));
//        System.out.println("End of test query");

//        QueryExecutionFactory sparqlService = SparqlServiceBuilder
//                .http("http://akswnc3.informatik.uni-leipzig.de:8860/sparql", "http://dbpedia.org")
//                .withPagination(100000)
//                .create();


        QueryExecutionFactory cachedQef = new QueryExecutionFactoryViewCacheMaster(rawQef, new HashMap<>()); //OpExecutorFactoryViewMatcher.get().getServiceMap());


        QueryExecutionFactory mainQef = new QueryExecutionFactoryCompare(rawQef, cachedQef);

        if(false) {
            CombinatoricsVector it = new CombinatoricsVector(5, 3);
            //CartesianVector it = new CartesianVector(5, 3);

            //it.inc(0);
            //it.inc(0);
            //it.inc(1);

            while(it.getVector() != null) {
                System.out.println(Arrays.toString(it.getVector()));
                it.inc();
            }

            System.exit(0);
        }

//        if(false) {
//            Query query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
//            cache.index(query);
//
//            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ex:bar ; ?s ?s . Filter(regex(str(?s), 'dbpedia')) .}");
//            cache.index(query);
//
//            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ?o .}");
//            cache.index(query);
//
//
//            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
//            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ?o; ex:bar ?z . }");
//            cache.execSelect(query);
//        }
//
//
//        // Ambiguous case:
//        if(false) {
//            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
//            Query query = QueryFactory.create("Select * { ?a ?b ?c . ?a ?d ?e }");
//            cache.index(query);
//
////            query = QueryFactory.create("Select * { ?a ?b ?c . ?d ?e ?f }");
////            cache.index(query);
//
//            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
//            query = QueryFactory.create("Select * { ?a ?b ?c . ?a ?d ?e . ?a ?f ?g . ?g ?h ?i . ?k ?l ?m . ?m ?n ?o }");
//            cache.execSelect(query);
//        }

        if(false) {
            Query query;
            QueryExecution qe;
            ResultSet rs;


            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
            query = QueryFactory.create("Select * { ?a a <http://dbpedia.org/ontology/Airport> }");

            qe = mainQef.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);


            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
            query = QueryFactory.create("Select * { ?a a <http://dbpedia.org/ontology/Airport> }");

            qe = mainQef.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);


            //cache.index(query);

//            query = QueryFactory.create("Select * { ?a ?b ?c . ?d ?e ?f }");
//            cache.index(query);

            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
            query = QueryFactory.create("Select * { ?x a <http://dbpedia.org/ontology/Airport> . ?x a <http://dbpedia.org/ontology/Place>  }");
            qe = mainQef.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);


            //cache.execSelect(query);
        }


        if(false) {
            String queryString = "Select ?s ?name  { ?s a <Person> . Optional { ?s <label> ?name . } }";
            Query query = QueryFactory.create(queryString);

            Op op = Algebra.compile(query);
            op = Algebra.toQuadForm(op);
            System.out.println(op);

            //query.setV
            //op = ReplaceConstants.replace(op);


            //TableData td = new TableData(variables, rows);
            //query.setValuesDataBlock(variables, values);
        }


        if(true) {
            QueryExecutionFactory ss = new QueryExecutionFactoryModel();

            Query query = QueryFactory.create("Select * { VALUES (?x) { ('foo') ('bar') } }");
            ResultSet rs = ss.createQueryExecution(query).execSelect();
            String str = ResultSetFormatter.asText(rs);
            System.out.println(str);

            Op op = Algebra.compile(query);
            op = Algebra.toQuadForm(op);
            System.out.println(op);
            query = OpAsQuery.asQuery(op);
            System.out.println(query);
        }

        //PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(); //TestBundleReader.class.getClass().getClassLoader());

        if(true) {
            Resource r = resolver.getResource("query-" + data + "-1a.sparql");
            String queryString = StreamUtils.toString(r.getInputStream());
            Query query = QueryFactory.create(queryString);

            Op op = Algebra.compile(query);
            op = Algebra.toQuadForm(op);
            System.out.println(op);

        }

        if(true) {




            for(int i = 0; i < 100; ++i) {


            Resource r;
            String queryString;
            Query query;
            QueryExecution qe;
            ResultSet rs;

            Stopwatch sw = Stopwatch.createStarted();

            long a = sw.elapsed(TimeUnit.MILLISECONDS);

            r = resolver.getResource("query-" + data + "-1a.sparql");
            queryString = StreamUtils.toString(r.getInputStream());
            query = QueryFactory.create(queryString);
            qe = mainQef.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);

            long b = sw.elapsed(TimeUnit.MILLISECONDS);
            logger.info("Time taken: " + (b - a));


            r = resolver.getResource("query-" + data + "-1b.sparql");
            queryString = StreamUtils.toString(r.getInputStream());
            query = QueryFactory.create(queryString);
            qe = mainQef.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);

            long c = sw.elapsed(TimeUnit.MILLISECONDS);
            logger.info("Time taken: " + (c - b));

            }
        }


        // We could use the projection to hack in some caching options on sub-queries...
        // Select (?cacheOptions = "?s") { ... }
    }

}


