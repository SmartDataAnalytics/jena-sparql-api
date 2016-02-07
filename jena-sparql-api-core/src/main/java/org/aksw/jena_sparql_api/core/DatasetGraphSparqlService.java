package org.aksw.jena_sparql_api.core;

import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.lookup.ListService;
import org.aksw.jena_sparql_api.lookup.ListServiceConcept;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.Quad;


public class DatasetGraphSparqlService
    extends DatasetGraphBaseFind
{
    //protected QueryExecutionFactory qef;
    protected SparqlService sparqlService;

    public DatasetGraphSparqlService(SparqlService sparqlService) {
        this.sparqlService = sparqlService;
    }

    public SparqlService getSparqlService() {
        return sparqlService;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        ListService<Concept, Node, Node> ls = new ListServiceConcept(qef);
        Set<Node> nodes = ls.fetchData(ConceptUtils.listAllGraphs, null, null).keySet();
        return nodes.iterator();
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, g, s, p, o);
        return result;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        Iterator<Quad> result = QueryExecutionUtils.findQuads(qef, Node.ANY, s, p, o);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        return null;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeGraph(Node graphName) {
        // TODO Auto-generated method stub

    }

}
