package org.aksw.jena_sparql_api.rdf.model.ext.dataset.api;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;

public interface DatasetGraphOneNg
    extends DatasetGraph
{
    Node getGraphNode();
}
