package org.aksw.jena_sparql_api;

import org.aksw.jena_sparql_api.core.GraphQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.junit.Test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class GraphTest {

    @Test
    public void sparql11Test() {
        Model baseModel = ModelFactory.createDefaultModel();

        Resource a = baseModel.createResource("http://example.org/Vehicle");
        Resource b = baseModel.createResource("http://example.org/Car");
        Resource c = baseModel.createResource("http://example.org/ElectricCar");

        baseModel.add(a, RDFS.subClassOf, OWL.Thing);
        baseModel.add(b, RDFS.subClassOf, a);
        baseModel.add(c, RDFS.subClassOf, b);

        QueryExecutionFactory baseQef = new QueryExecutionFactoryModel(baseModel);

        Graph graph = new GraphQueryExecutionFactory(baseQef);

        Model model = ModelFactory.createModelForGraph(graph);

        QueryExecutionFactory qef = new QueryExecutionFactoryModel(model);

        QueryExecution qe = qef.createQueryExecution("Select Distinct ?s { ?s <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ <http://www.w3.org/2002/07/owl#Thing> }");
        ResultSet rs = qe.execSelect();
        ResultSetFormatter.out(System.out, rs);
    }
}
