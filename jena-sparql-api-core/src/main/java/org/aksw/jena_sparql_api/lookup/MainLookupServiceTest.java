package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;

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
