package org.aksw.jena_sparql_api_sparql_path.main;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Concept;
import org.aksw.jena_sparql_api.sparql_path.core.domain.Path;


public class TestMain {
    public static void main(String[] args) {
//        CacheFrontend cacheFrontend = WebMvcConfig.createSparqlCache();
//        SparqlServiceFactory sparqlServiceFactory = new SparqlServiceFactoryImpl(cacheFrontend);
        
        Set<String> defaultGraphs = Collections.emptySet();
        //QueryExecutionFactory qef = sparqlServiceFactory.createSparqlService("http://localhost:8801/sparql", defaultGraphs);
        QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://localhost:8801/sparql");
        
        Concept sourceConcept = Concept.create("?s ?p ?o", "s");
        //Concept targetConcept = Concept.create("?x ?s ?y . Filter(regex(str(?s), 'super', 'i'))", "x");
        
        //Concept targetConcept = Concept.create("?s ?x ?y . Filter(regex(str(?x), 'super', 'i'))", "s");
        //Concept targetConcept = Concept.create("?a ?b ?c . Filter(regex(str(?b), 'super', 'i'))", "a");
        
        //Concept targetConcept = Concept.create("?x ?s ?y . ?s <http://http://www.w3.org/2000/01/rdf-schema#label> ?l . Filter(regex(str(?l), 'super', 'i'))", "x");

        //Concept targetConcept = Concept.create("?g <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?x ; <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?y", "g");

        Concept targetConcept = Concept.create("?g ?y ?x . Filter(?y = <http://www.w3.org/2003/01/geo/wgs84_pos#lat> || ?y = <http://www.w3.org/2003/01/geo/wgs84_pos#long>)", "g");
        
        List<Path> paths = ConceptPathFinder.findPaths(qef, sourceConcept, targetConcept, 10, 10);
        
        System.out.println("Got " + paths.size() + " results:");
        for(Path path : paths) {
            System.out.println(path);
        }
    }
}
