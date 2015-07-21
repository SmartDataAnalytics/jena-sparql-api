package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.concept_cache.core.QueryExecutionFactoryConceptCache;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.vocabulary.RDF;

public class ConceptCache {

    private ConceptMap conceptMap = new ConceptMap();



    private Map<Concept, List<Resource>> extension = new HashMap<Concept, List<Resource>>();
    private QueryExecutionFactory sparqlService;

    public ConceptCache(QueryExecutionFactory sparqlService) {
        this.sparqlService = sparqlService;
    }

    public static void applyCache() {
        // We can iterate the triple patterns (based on selectivity)
        // and make lookups of which triple patterns match


    }




    public void index(Query query, ResultSet rs) {

        conceptMap.index(query, rs);
    }

    public void index(Query query) {

        String varName = "a";
        Var v = Var.alloc(varName);
        List<String> varList = Arrays.asList(varName);
        List<Binding> bindings = new ArrayList<Binding>();

        BindingHashMap binding = new BindingHashMap();
        binding.add(v, RDF.type.asNode());

        bindings.add(binding);

        QueryIterator queryIter = new QueryIterPlainWrapper(bindings.iterator());

        ResultSet rs = ResultSetFactory.create(queryIter, varList);
        rs = ResultSetFactory.copyResults(rs);



        //VarUtils.

        //Set<Var> v

        conceptMap.index(query, rs);
    }

    public ResultSet execSelect(Query query) {

        conceptMap.lookup(query);
        //RestrictionManagerI


        //OpQopFilter.getSubOp();

        //System.out.println(op);
        //System.out.println(quads);
        //System.out.println(rm);

        return null;
    }



    public static void main2(String[] args) throws IOException {

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("data-lorenz.nt");

        String fileName = resource.getFilename();

        Model model = RDFDataMgr.loadModel(fileName);
        QueryExecutionFactory sparqlService = FluentQueryExecutionFactory
            .model(model)
            .config()
                .withPagination(100000)
            .end()
            .create();


//        QueryExecutionFactory sparqlService = SparqlServiceBuilder
//                .http("http://akswnc3.informatik.uni-leipzig.de:8860/sparql", "http://dbpedia.org")
//                .withPagination(100000)
//                .create();

        ConceptCache cache = new ConceptCache(sparqlService);


        sparqlService = new QueryExecutionFactoryConceptCache(sparqlService);


        if(false) {
            CombinatoricsVector it = new CombinatoricsVector(5, 3);
            //CartesianVector it = new CartesianVector(5, 3);

            //it.inc(0);
            //it.inc(0);
            //it.inc(1);

            while(it.vector() != null) {
                System.out.println(Arrays.toString(it.vector()));
                it.inc();
            }

            System.exit(0);
        }

        if(false) {
            Query query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
            cache.index(query);

            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ex:bar ; ?s ?s . Filter(regex(str(?s), 'dbpedia')) .}");
            cache.index(query);

            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ?o .}");
            cache.index(query);


            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
            query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport ; ex:foo ?o; ex:bar ?z . }");
            cache.execSelect(query);
        }


        // Ambiguous case:
        if(false) {
            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
            Query query = QueryFactory.create("Select * { ?a ?b ?c . ?a ?d ?e }");
            cache.index(query);

//            query = QueryFactory.create("Select * { ?a ?b ?c . ?d ?e ?f }");
//            cache.index(query);

            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
            query = QueryFactory.create("Select * { ?a ?b ?c . ?a ?d ?e . ?a ?f ?g . ?g ?h ?i . ?k ?l ?m . ?m ?n ?o }");
            cache.execSelect(query);
        }

        if(false) {
            Query query;
            QueryExecution qe;
            ResultSet rs;


            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
            query = QueryFactory.create("Select * { ?a a <http://dbpedia.org/ontology/Airport> }");

            qe = sparqlService.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);


            //Query query = QueryFactory.create("Select * { ?a ?b ?c . ?c ?d ?e }");
            query = QueryFactory.create("Select * { ?a a <http://dbpedia.org/ontology/Airport> }");

            qe = sparqlService.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);


            //cache.index(query);

//            query = QueryFactory.create("Select * { ?a ?b ?c . ?d ?e ?f }");
//            cache.index(query);

            //query = QueryFactory.create("Prefix ex: <http://example.com/> Select * { ?s a ex:Airport . Filter(regex(str(?s), 'dbpedia')) .}");
            query = QueryFactory.create("Select * { ?x a <http://dbpedia.org/ontology/Airport> . ?x a <http://dbpedia.org/ontology/Place>  }");
            qe = sparqlService.createQueryExecution(query);
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
            Resource r = resolver.getResource("query-lorenz-1a.sparql");
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

            r = resolver.getResource("query-lorenz-1a.sparql");
            queryString = StreamUtils.toString(r.getInputStream());
            query = QueryFactory.create(queryString);
            qe = sparqlService.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);

            long b = sw.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Time taken: " + (b - a));


            r = resolver.getResource("query-lorenz-1b.sparql");
            queryString = StreamUtils.toString(r.getInputStream());
            query = QueryFactory.create(queryString);
            qe = sparqlService.createQueryExecution(query);
            rs = qe.execSelect();
            ResultSetFormatter.consume(rs);

            long c = sw.elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Time taken: " + (c - b));

            }
        }


        // We could use the projection to hack in some caching options on sub-queries...
        // Select (?cacheOptions = "?s") { ... }
    }

}


