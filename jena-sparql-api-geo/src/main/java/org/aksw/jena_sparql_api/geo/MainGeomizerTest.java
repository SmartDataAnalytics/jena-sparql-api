package org.aksw.jena_sparql_api.geo;

import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceCacheMem;
import org.aksw.jena_sparql_api.lookup.LookupServicePartition;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.utils.TripleUtils;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import com.vividsolutions.jts.geom.Geometry;

//Query query = QueryFactory.create("Prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> Prefix dbpo: <http://dbpedia.org/ontology/> Select ?s ?w { ?s a dbpo:Castle ; geo:geometry ?w }");


public class MainGeomizerTest {

    public static LookupService<Node, Geometry> createLookupService(QueryExecutionFactory sparqlService, MappedConcept<Geometry> mc) {
        //LookupService<Node, Geometry> result = LookupServiceUtilsGeo.createGeoLookupService(sparqlService, mc);
        LookupService<Node, Geometry> result = LookupServiceUtils.createLookupService(sparqlService, mc);
        result = LookupServicePartition.create(result, 30, 5);
        result = LookupServiceCacheMem.create(result);

        return result;
    }


    public static void main(String[] args) {



        //Model model = FileManager.get().loadModel("/home/raven/Projects/Eclipse/24-7-platform/link-specifications/dbpedia-linkedgeodata-university/positive.nt");
        Model model = FileManager.get().loadModel("/home/raven/Projects/Eclipse/24-7-platform/link-specifications/dbpedia-linkedgeodata-airport/positive.nt");

        Graph graph = model.getGraph();
        Set<Triple> triples = graph.find(null, null, null).toSet();

        triples = TripleUtils.swap(triples);

        QueryExecutionFactory sparqlServiceSource = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
        QueryExecutionFactory sparqlServiceTarget = new QueryExecutionFactoryHttp("http://linkedgeodata.org/sparql", "http://linkedgeodata.org");

        /*
        LookupService<Node, ResultSet> ls = new LookupServiceSparqlQuery(sparqlService, query, var);
        ls = LookupServicePartition.create(ls, 1);
        ls = LookupServiceCacheMem.create(ls);
        */

        MappedConcept<Geometry> mcA = GeoMapSupplierUtils.mcWgsGeometry;
        MappedConcept<Geometry> mcB = GeoMapSupplierUtils.mcOgcGeometry;

        LookupService<Node, Geometry> lsA = createLookupService(sparqlServiceSource, mcA);
        LookupService<Node, Geometry> lsB = createLookupService(sparqlServiceTarget, mcB);

//        Collection<Node> test = Collections.singletonList(NodeFactory.createURI("http://linkedgeodata.org/triplify/node404580641"));
//        Map<Node, Geometry> testResult = lsB.apply(test);
//        System.out.println("YAY: " + testResult);
//
//        if(true) {
//            return;
//        }

        Map<Triple, Geometry> map = LinkGeomizer.geomize(triples, lsA, lsB);

        Model result = ModelFactory.createDefaultModel();
        Set<Triple> ts = GeoMapSupplierUtils.geomizedToRdf(map);
        GraphUtil.add(result.getGraph(), ts.iterator());

        result.write(System.out, "N-TRIPLES");


    }
}
