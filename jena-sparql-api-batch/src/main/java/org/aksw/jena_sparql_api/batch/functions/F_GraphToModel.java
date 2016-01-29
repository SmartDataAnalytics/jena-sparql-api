package org.aksw.jena_sparql_api.batch.functions;

import com.google.common.base.Function;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

class F_GraphToModel
    implements Function<Graph, Model>
{
    @Override
    public Model apply(Graph graph) {
        Model result = ModelFactory.createModelForGraph(graph);
        return result;
    }

    public static final F_GraphToModel fn = new F_GraphToModel();
}