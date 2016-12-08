package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.utils.ResultSetPart;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;

// TODO Move to some test suite
public class MainLookupServiceTest {
    public static void main(String[] args) {
        QueryExecutionFactory sparqlService = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");

        Var var = Var.alloc("s");
        Query query = QueryFactory.create("Prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> Prefix dbpo: <http://dbpedia.org/ontology/> Select ?s ?w { ?s a dbpo:Castle ; geo:geometry ?w }");

        LookupService<Node, ResultSetPart> ls = new LookupServiceSparqlQuery(sparqlService, query, var);
        ls = LookupServicePartition.create(ls, 1);
        ls = LookupServiceCacheMem.create(ls);

        List<Node> keys = new ArrayList<Node>();
        keys.add(NodeFactory.createURI("http://dbpedia.org/resource/Marksburg"));
        keys.add(NodeFactory.createURI("http://dbpedia.org/resource/Rosenburg"));

        Map<Node, ResultSetPart> map = ls.apply(keys);

        System.out.println(map);
    }
}
