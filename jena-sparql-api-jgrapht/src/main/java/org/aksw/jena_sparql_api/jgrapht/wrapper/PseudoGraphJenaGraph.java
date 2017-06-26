package org.aksw.jena_sparql_api.jgrapht.wrapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;


/**
 * Wrapper for exposing a Jena graph as a JGraphT directed pseudo graph.
 *
 * Note: All graph lookups are done via a .find() method that does additional filtering for supporting
 * variables as vertices.
 *
 *
 * @author raven
 *
 */
public class PseudoGraphJenaGraph
    implements DirectedGraph<Node, Triple>
{
    protected org.apache.jena.graph.Graph graph;

   /**
     * Predicate to which to confine the underlying Jena graph. May be Node.ANY
     * to use all triples regardless to their predicate.
     *
     */
    protected Node confinementPredicate; // May be Node.ANY

    protected EdgeFactory<Node, Triple> edgeFactory;

    public PseudoGraphJenaGraph(Graph graph) {
        this(graph, Node.ANY, null);
    }

    public PseudoGraphJenaGraph(Graph graph, Node confinementPredicate, Node insertPredicate) {
        super();
        this.graph = graph;
        this.confinementPredicate = confinementPredicate;

        edgeFactory = new EdgeFactoryJenaGraph(insertPredicate);
    }


    @Override
    public Set<Triple> getAllEdges(Node sourceVertex, Node targetVertex) {
        return find(graph, sourceVertex, confinementPredicate, targetVertex).toSet();
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
        Triple result = edgeFactory.createEdge(sourceVertex, targetVertex);
        graph.add(result);

        return result;
    }

    @Override
    public boolean addEdge(Node sourceVertex, Node targetVertex, Triple e) {
        boolean isValid = e.getSubject().equals(sourceVertex) && e.getObject().equals(targetVertex);
        if(!isValid) {
            throw new RuntimeException("Source and/or target vertex does not match those of the triple: " + sourceVertex + " " + targetVertex + " " + e);
        }

        if(!confinementPredicate.equals(Node.ANY) && e.getPredicate().equals(confinementPredicate)) {
            throw new RuntimeException("Graph is confined to predicate " + confinementPredicate + " therefore cannot add edge with predicate " + e);
        }

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
        boolean result = find(graph, sourceVertex, confinementPredicate, targetVertex).hasNext();
        return result;
    }

    @Override
    public boolean containsEdge(Triple e) {
        boolean result = find(graph, e.getSubject(), e.getPredicate(), e.getObject()).hasNext();
        return result;
    }

    @Override
    public boolean containsVertex(Node v) {
        boolean result =
                find(graph, v, confinementPredicate, Node.ANY).hasNext() ||
                find(graph, Node.ANY, confinementPredicate, v).hasNext();
        return result;
    }

    @Override
    public Set<Triple> edgeSet() {
        Set<Triple> result = find(graph, Node.ANY, confinementPredicate, Node.ANY).toSet();
        return result;
    }

    @Override
    public Set<Triple> edgesOf(Node vertex) {
        Set<Triple> result = new HashSet<>();
        find(graph, vertex, confinementPredicate, Node.ANY).forEachRemaining(result::add);
        find(graph, Node.ANY, confinementPredicate, vertex).forEachRemaining(result::add);

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
        graph.remove(sourceVertex, confinementPredicate, targetVertex);
        return null;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends Node> vertices) {
        boolean result = false;
        for(Node v : vertices) {
            result = result || removeVertex(v);
        }

        return result;
    }

    @Override
    public Triple removeEdge(Node sourceVertex, Node targetVertex) {
        Triple result = new Triple(sourceVertex, confinementPredicate, targetVertex);
        removeEdge(result);
//    	graph.remove(result.get, p, o);
//        return true;
        return null;
    }

    @Override
    public boolean removeEdge(Triple e) {
        if(!e.getPredicate().equals(confinementPredicate) && !confinementPredicate.equals(Node.ANY)) {
            throw new RuntimeException("Cannot remove edge outside of confinement - predicate must be: " + confinementPredicate + " but got " + e);
        }

        graph.remove(e.getSubject(), e.getPredicate(), e.getObject());
        return true;
    }


    @Override
    public boolean removeVertex(Node v) {
        graph.remove(v, confinementPredicate, Node.ANY);
        graph.remove(Node.ANY, confinementPredicate, v);
        // FIXME Return proper result
        return true;
    }


    @Override
    public Set<Node> vertexSet() {
        Set<Node> result = new HashSet<>();
        find(graph, Node.ANY, confinementPredicate, Node.ANY).forEachRemaining(triple -> {
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

    /**
     * FIXME: We could delegate requests to edge weights to a lambda which e.g. gets this value from the RDF
     */
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
        Set<Triple> result = find(graph, Node.ANY, confinementPredicate, vertex).toSet();
        return result;
    }

    @Override
    public int outDegreeOf(Node vertex) {
        int result = outgoingEdgesOf(vertex).size();
        return result;
    }

    @Override
    public Set<Triple> outgoingEdgesOf(Node vertex) {
        Set<Triple> result = find(graph, vertex, confinementPredicate, Node.ANY).toSet();
        return result;
    }


    /**
     * A delegate to find - single point for adding any post processing should it become necessary
     *
     * @param graph
     * @param s
     * @param p
     * @param o
     * @return
     */
    public static ExtendedIterator<Triple> find(Graph graph, Node s, Node p, Node o) {
//  Filter used to allow matches by variable names - vars are now handled by GraphVar
//        ExtendedIterator<Triple> result = graph.find(s, p, o).filterKeep(t -> {
//            boolean r =
//                    (s.equals(Node.ANY) ? true : t.getSubject().equals(s)) &&
//                    (p.equals(Node.ANY) ? true : t.getPredicate().equals(p)) &&
//                    (o.equals(Node.ANY) ? true : t.getObject().equals(o));
//            return r;
//        });

        return graph.find(s, p, o);
    }

    @Override
    public String toString() {
        return "PseudoGraphJenaGraph [graph=" + graph + ", confinementPredicate=" + confinementPredicate
                + ", edgeFactory=" + edgeFactory + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((confinementPredicate == null) ? 0 : confinementPredicate.hashCode());
        result = prime * result + ((edgeFactory == null) ? 0 : edgeFactory.hashCode());
        result = prime * result + ((graph == null) ? 0 : graph.hashCode());
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
        PseudoGraphJenaGraph other = (PseudoGraphJenaGraph) obj;
        if (confinementPredicate == null) {
            if (other.confinementPredicate != null)
                return false;
        } else if (!confinementPredicate.equals(other.confinementPredicate))
            return false;
        if (edgeFactory == null) {
            if (other.edgeFactory != null)
                return false;
        } else if (!edgeFactory.equals(other.edgeFactory))
            return false;
        if (graph == null) {
            if (other.graph != null)
                return false;
        } else if (!graph.equals(other.graph))
            return false;
        return true;
    }

}
