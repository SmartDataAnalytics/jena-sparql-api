package org.aksw.jena_sparql_api.playground;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.Path;

class TaskContext {
    protected String description;
    protected SparqlService sparqlService;
    //protected SparqlPathFinder pathFinder;
    protected Path path;
    protected int k;
    //protected QueryExecutionFactory joinSummary;
    protected Node startNode;
    protected Node endNode;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public SparqlService getSparqlService() {
        return sparqlService;
    }
    public void setSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
    }
    public Path getPath() {
        return path;
    }
    public void setPath(Path path) {
        this.path = path;
    }
    public int getK() {
        return k;
    }
    public void setK(int k) {
        this.k = k;
    }
    public Node getStartNode() {
        return startNode;
    }
    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }
    public Node getEndNode() {
        return endNode;
    }
    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }
}