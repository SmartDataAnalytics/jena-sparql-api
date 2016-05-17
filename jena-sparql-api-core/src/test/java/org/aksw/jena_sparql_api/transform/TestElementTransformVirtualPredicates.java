package org.aksw.jena_sparql_api.transform;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.junit.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.algebra.optimize.TransformFilterPlacement;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDFS;

public class TestElementTransformVirtualPredicates {


    public static void main(String[] args) {
        TestElementTransformVirtualPredicates x = new TestElementTransformVirtualPredicates();
        x.test();
    }

    @Test
    public void test() {
        Map<Node, Relation> virtualPredicates = new HashMap<Node, Relation>();

        //virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("GRAPH ?g { ?s ?p ?o } . ?g <http://owner> ?o", "s", "o"));
        //virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("?s <test> ?g . ?g <http://owner> ?o", "s", "o"));



        virtualPredicates.put(NodeFactory.createURI(RDFS.label.getURI()), Relation.create("?s <skos:label> [ <skos:value> ?l]", "s", "l"));


        //Query query = QueryFactory.create("Select * { ?s <http://ex.org/label> ?o }");
        Query finalQuery = QueryFactory.create("Select * { ?s ?p ?o . Filter(?s = <http://ex.org/foo>) }");
        //op = Transformer.transform(new TransformFilterPlacement(), op);




        Query intermediateQuery = ElementTransformVirtualPredicates.transform(finalQuery, virtualPredicates, true);

        // Optimize the query
        Op op = Algebra.compile(intermediateQuery);

        Context ctx = ARQ.getContext().copy();

        // Disable this transformation, as it does not generate valid SPARQL syntax
        ctx.set(ARQ.optFilterEquality, false);
        System.out.println("status: " + ctx.get(ARQ.optFilterEquality));


//        Context ctx = new Context();
//        ctx.put(ARQ.optMergeBGPs, true);
//        ctx.put(ARQ.optMergeExtends, true);
//        ctx.put(ARQ.optExprConstantFolding, true);
//        ctx.put(ARQ.optFilterPlacement, true);
//        ctx.put(ARQ.optFilterConjunction, true);
//        ctx.put(ARQ.optImplicitLeftJoin, true);
//        ctx.put(ARQ.optFilterEquality, false);
//        ctx.put(ARQ.optFilterInequality, false);
//        ctx.put(ARQ.optDistinctToReduced, false);
//        ctx.put(ARQ.optFilterExpandOneOf, false);
//        ctx.put(ARQ.optFilterPlacementBGP, false);


//        op = Optimize.optimize(op, ctx);
        System.out.println(op);
        finalQuery = OpAsQuery.asQuery(op);

        System.out.println("Rewritten query: " + finalQuery);
    }
}
