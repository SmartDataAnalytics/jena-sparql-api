package org.aksw.jena_sparql_api_sparql_path2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;


/**
 * Wrapper for exposing a Jena graph as a JGraphT directed pseudo graph.
 *
 *
 *
 * @author raven
 *
 */
public class PseudoGraphJena
    implements DirectedGraph<Node, Triple>
{
    protected org.apache.jena.graph.Graph graph;
    protected Node predicate; // May be Node.ANY

    protected transient EdgeFactory<Node, Triple> edgeFactory;

    public PseudoGraphJena(Graph graph, Node predicate) {
        super();
        this.graph = graph;
        this.predicate = predicate;

        edgeFactory = new EdgeFactoryJena(predicate);
    }


    @Override
    public Set<Triple> getAllEdges(Node sourceVertex, Node targetVertex) {
        return graph.find(sourceVertex, predicate, targetVertex).toSet();
    }

    @Override
    public Triple getEdge(Node sourceVertex, Node targetVertex) {
        Set<Triple> edges = getAllEdges(sourceVertex, targetVertex);
        // TODO Maybe throw an exception or return null if there are multiple edges
        Triple result = edges.iterator().next();
        return result;
    }

    @Override
    public EdgeFactory<Node, Triple> getEdgeFactory() {
        return edgeFactory;
    }

    @Override
    public Triple addEdge(Node sourceVertex, Node targetVertex) {
        Triple result;
        if(Node.ANY.equals(predicate)) {
            throw new UnsupportedOperationException("Cannot insert edge if the predicate is Node.ANY");
        } else {
            result = new Triple(sourceVertex, predicate, targetVertex);
            graph.add(result);
        }
        return result;
    }

    @Override
    public boolean addEdge(Node sourceVertex, Node targetVertex, Triple e) {
        boolean result = !graph.contains(e);
        if(result) {
            graph.add(e);
        }
        return true;
    }

    @Override
    public boolean addVertex(Node v) {
        // Silently ignore calls to this
        return false;
    }

    @Override
    public boolean containsEdge(Node sourceVertex, Node targetVertex) {
        // TODO Not sure if contains works with Node.ANY - may have to use !.find().toSet().isEmpyt()
        boolean result = graph.contains(sourceVertex, predicate, targetVertex);
        return result;
    }

    @Override
    public boolean containsEdge(Triple e) {
        boolean result = graph.contains(e);
        return result;
    }

    @Override
    public boolean containsVertex(Node v) {
        boolean result =
                graph.contains(v, Node.ANY, Node.ANY) ||
                graph.contains(Node.ANY, Node.ANY, v);
        return result;
    }

    @Override
    public Set<Triple> edgeSet() {
        Set<Triple> result = graph.find(Node.ANY, Node.ANY, Node.ANY).toSet();
        return result;
    }

    @Override
    public Set<Triple> edgesOf(Node vertex) {
        Set<Triple> result = new HashSet<>();
        graph.find(vertex, predicate, Node.ANY).forEachRemaining(result::add);
        graph.find(Node.ANY, predicate, vertex).forEachRemaining(result::add);

        return result;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends Triple> edges) {
        Iterator<Triple> it = edges.stream().map(e -> (Triple)e).iterator();
        GraphUtil.delete(graph, it);
        return true;
    }

    @Override
    public Set<Triple> removeAllEdges(Node sourceVertex, Node targetVertex) {
        graph.remove(sourceVertex, Node.ANY, targetVertex);
        return null;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Node> vertices) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Triple removeEdge(Node sourceVertex, Node targetVertex) {
        Triple result = new Triple(sourceVertex, predicate, targetVertex);
        removeEdge(result);
//    	graph.remove(result.get, p, o);
//        return true;
        return null;
    }

    @Override
    public boolean removeEdge(Triple e) {
        graph.remove(e.getSubject(), e.getPredicate(), e.getObject());
        return true;
    }


    @Override
    public boolean removeVertex(Node v) {
        graph.remove(v, predicate, Node.ANY);
        graph.remove(Node.ANY, predicate, v);
        return true;
    }


    @Override
    public Set<Node> vertexSet() {
        Set<Node> result = new HashSet<>();
        graph.find(Node.ANY, predicate, Node.ANY).forEachRemaining(triple -> {
                result.add(triple.getSubject());
                result.add(triple.getObject());
        });
        return result;
    }

    @Override
    public Node getEdgeSource(Triple e) {
        return e.getSubject();
    }

    @Override
    public Node getEdgeTarget(Triple e) {
        return e.getObject();
    }

    @Override
    public double getEdgeWeight(Triple e) {
        return 1;
    }

    @Override
    public int inDegreeOf(Node vertex) {
        int result = incomingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Triple> incomingEdgesOf(Node vertex) {
        Set<Triple> result = graph.find(Node.ANY, predicate, vertex).toSet();
        return result;
    }

    @Override
    public int outDegreeOf(Node vertex) {
        int result = outgoingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Triple> outgoingEdgesOf(Node vertex) {
        Set<Triple> result = graph.find(vertex, predicate, Node.ANY).toSet();
        return result;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((graph == null) ? 0 : graph.hashCode());
        result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PseudoGraphJena other = (PseudoGraphJena) obj;
        if (graph == null) {
            if (other.graph != null)
                return false;
        } else if (!graph.equals(other.graph))
            return false;
        if (predicate == null) {
            if (other.predicate != null)
                return false;
        } else if (!predicate.equals(other.predicate))
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "PseudoGraphJena [graph=" + graph + ", predicate=" + predicate + "]";
    }
}
