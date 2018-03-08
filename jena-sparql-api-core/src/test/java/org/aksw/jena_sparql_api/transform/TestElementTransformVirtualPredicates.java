package org.aksw.jena_sparql_api.transform;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestElementTransformVirtualPredicates {


    public static void main(String[] args) {
        TestElementTransformVirtualPredicates x = new TestElementTransformVirtualPredicates();
        x.test();
    }

    @Test
    public void test() {
    	// Set up some default namespaces
    	Prologue prologue = new Prologue();
        prologue.setPrefixMapping(PrefixMapping.Extended);
        
        // Load a simple RDF model about people and their birth date
        // Note, that there is no explicit 'age' attribute in the data
        Model model = RDFDataMgr.loadModel("virtual-predicates-example.ttl");
        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);


        // Set up a map for expanding predicates with binary (sparql) relations
    	Map<Node, BinaryRelation> virtualPredicates = new HashMap<Node, BinaryRelation>();

    	// Register a virtual predicate that computes the age from the current
    	// date and birth date of a person
        virtualPredicates.put(NodeFactory.createURI("http://www.example.org/age"),
        		BinaryRelation.create("?s a eg:Person ; eg:birthDate ?start . " + 
        		        "BIND(NOW() AS ?end) " +
        				"BIND(YEAR(?end) - YEAR(?start) - IF(MONTH(?end) < MONTH(?start) || (MONTH(?end) = MONTH(?start) && DAY(?end) < DAY(?start)), 1, 0) as ?age)",
                        "s", "age", prologue));

        
        // Set up some queries and run them
        SparqlQueryParser parser = SparqlQueryParserImpl.create(Syntax.syntaxARQ, prologue);
        
        List<Query> queries = Arrays.asList(
            parser.apply("Select (year(NOW()) - year('1984-01-01'^^xsd:date) AS ?d) { }"),
        	parser.apply("Select * { ?s ?p ?o  }"),
        	parser.apply("Select * { ?s eg:age ?o  }"),
        	parser.apply("Select * { ?s a eg:Person ; eg:age ?a }"),
        	parser.apply("Select * { ?s a eg:Person ; ?p ?o . FILTER(?p = eg:age) }")
        );

        for(Query query : queries) {
        	if(query.isQueryResultStar()) {
	        	query.getProjectVars().addAll(query.getResultVars().stream().map(Var::alloc).collect(Collectors.toList()));
	        	query.setQueryResultStar(false);
	        	System.out.println(query);
        	}

        	Query intermediateQuery = ElementTransformVirtualPredicates.transform(query, virtualPredicates, true);
        	
            Op op = Algebra.compile(intermediateQuery);

            Context ctx = ARQ.getContext().copy();

            // Disable this transformation, as it does not generate valid SPARQL syntax
            ctx.set(ARQ.optFilterEquality, false);
            //System.out.println("status: " + ctx.get(ARQ.optFilterEquality));


//            Context ctx = new Context();
//            ctx.put(ARQ.optMergeBGPs, true);
//            ctx.put(ARQ.optMergeExtends, true);
//            ctx.put(ARQ.optExprConstantFolding, true);
//            ctx.put(ARQ.optFilterPlacement, true);
//            ctx.put(ARQ.optFilterConjunction, true);
//            ctx.put(ARQ.optImplicitLeftJoin, true);
//            ctx.put(ARQ.optFilterEquality, false);
//            ctx.put(ARQ.optFilterInequality, false);
//            ctx.put(ARQ.optDistinctToReduced, false);
//            ctx.put(ARQ.optFilterExpandOneOf, false);
            ctx.put(ARQ.optFilterPlacement, true);
            ctx.put(ARQ.optFilterPlacementBGP, true);
            
            // TODO Implement rewrite to pull up
            //  extends over joins (join(..., extends(...), ...) -> extends(join(...))
            // Then apply merge BGP
            //ctx.put(ARQ.opt);
            
            

            //op = Optimize.optimize(op, ctx);
            System.out.println(op);
            Query finalQuery = OpAsQuery.asQuery(op);

            System.out.println("Rewritten query: " + finalQuery);

            System.out.println(ResultSetFormatter.asText(
            		FluentQueryExecutionFactory
            			.from(model).create().createQueryExecution(finalQuery).execSelect()));
            
        }
        
        
        //virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("GRAPH ?g { ?s ?p ?o } . ?g <http://owner> ?o", "s", "o"));
        //virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("?s <test> ?g . ?g <http://owner> ?o", "s", "o"));



        virtualPredicates.put(NodeFactory.createURI(RDFS.label.getURI()), BinaryRelation.create("?s <skos:label> [ <skos:value> ?l]", "s", "l"));


        //Query query = QueryFactory.create("Select * { ?s <http://ex.org/label> ?o }");
//        Query finalQuery = QueryFactory.create("Select * { ?s ?p ?o . Filter(?s = <http://ex.org/foo>) }");
        //op = Transformer.transform(new TransformFilterPlacement(), op);




  //      Query intermediateQuery = ElementTransformVirtualPredicates.transform(finalQuery, virtualPredicates, true);

        // Optimize the query

    }
}
