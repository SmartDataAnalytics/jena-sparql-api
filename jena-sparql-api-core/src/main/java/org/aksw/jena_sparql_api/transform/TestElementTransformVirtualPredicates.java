package org.aksw.jena_sparql_api.transform;

import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

public class TestElementTransformVirtualPredicates {

    @Test
    public void test() {
        Map<Node, Relation> virtualPredicates = new HashMap<Node, Relation>();
        virtualPredicates.put(NodeFactory.createURI("http://ex.org/label"), Relation.create("?s skos:label [ skos: value ?l]", "s", "l"));

        Query query = QueryFactory.create("Select * { ?s <http://ex.org/label> ?o }");

        Query actual = ElementTransformVirtualPredicates.transform(query, virtualPredicates, true);
        System.out.println(actual);

    }
}
