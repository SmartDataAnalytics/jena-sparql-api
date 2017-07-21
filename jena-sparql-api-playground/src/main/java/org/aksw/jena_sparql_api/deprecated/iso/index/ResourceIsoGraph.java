package org.aksw.jena_sparql_api.deprecated.iso.index;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ResourceIsoGraph
    extends ResourceImpl
{
    public ResourceIsoGraph(Node node, EnhGraph graph) {
        super(node, graph);
    }

//	getIsoGraph()
}
