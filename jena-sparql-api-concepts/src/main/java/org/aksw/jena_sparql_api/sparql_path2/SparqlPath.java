package org.aksw.jena_sparql_api.sparql_path2;

import java.util.List;

import org.apache.jena.graph.Node;

class PropertyStep {
    Node property;
    boolean isInverse;
}

class PathStep {
    PropertyStep propertyStep;
    Node targetNode;
}

public class SparqlPath {
    protected Node startNode;
    protected List<PathStep> steps;
    //protected Node endNode;
}
