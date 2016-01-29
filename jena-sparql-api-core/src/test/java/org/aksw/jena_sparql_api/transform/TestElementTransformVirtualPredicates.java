package org.aksw.jena_sparql_api.transform;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.junit.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class TestElementTransformVirtualPredicates {

    @Test
    public void test() {
        Map<Node, Relation> virtualPredicates = new HashMap<Node, Relation>();
        virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("?s <skos:label> [ <skos:value> ?l]", "s", "l"));

        //Query query = QueryFactory.create("Select * { ?s <http://ex.org/label> ?o }");

        Query query = QueryFactory.create("Select * { ?s ?p ?o . ?a ?b ?c .Filter(?p = <http://ex.org/label>) }");

        Query actual = ElementTransformVirtualPredicates.transform(query, virtualPredicates, true);
        System.out.println(actual);
    }
}
