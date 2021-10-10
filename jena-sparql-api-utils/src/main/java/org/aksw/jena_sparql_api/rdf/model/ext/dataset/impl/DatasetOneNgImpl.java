package org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetGraphOneNg;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetOneNg;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.graph.GraphFactory;

public class DatasetOneNgImpl
    extends DatasetImpl
    implements DatasetOneNg
{
    public DatasetOneNgImpl(DatasetGraphOneNg dsg) {
        super(dsg);
    }

    public static DatasetOneNg wrap(DatasetGraphOneNg dsg) {
        return new DatasetOneNgImpl(dsg);
    }

    public static DatasetOneNg create(String graphName) {
        return create(graphName, GraphFactory.createDefaultGraph());
    }

    public static DatasetOneNg create(String graphName, Graph graph) {
        return wrap(DatasetGraphOneNgImpl.create(NodeFactory.createURI(graphName), graph));
    }

    @Override
    public String getGraphName() {
        return ((DatasetGraphOneNg)dsg).getGraphNode().getURI();
    }
}
