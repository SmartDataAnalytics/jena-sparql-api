package org.aksw.jena_sparql_api.utils.model;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ResourceUtils {

    public static Resource asResource(Node node, Graph graph) {
        Model model = ModelFactory.createModelForGraph(graph);
        RDFNode tmp = org.apache.jena.sparql.util.ModelUtils.convertGraphNodeToRDFNode(node, model);
        Resource result = tmp.asResource();
        return result;
    }


    public static void addLiteral(Resource r, Property p, Object o) {
        if(o != null) {
            r.addLiteral(p, o);
        }
    }

    public static void addProperty(Resource r, Property p, RDFNode o) {
        if(o != null) {
            r.addProperty(p, o);
        }
    }
}
