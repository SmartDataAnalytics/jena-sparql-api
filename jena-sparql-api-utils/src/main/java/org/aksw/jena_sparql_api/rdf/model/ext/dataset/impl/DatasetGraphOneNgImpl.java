package org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl;

import java.util.Collections;
import java.util.Iterator;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.DatasetGraphOneNg;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.NullIterator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TxnDataset2Graph;
import org.apache.jena.sparql.graph.GraphOps;
import org.apache.jena.sparql.graph.GraphZero;

/** Adapted from DatasetGraphOne ~ Claus Stadler
 *
 *  DatasetGraph of a single named graph graph.
 * <p>
 *  Fixed as one graph (the default) - named graphs can not be added nor the default graph changed, only the contents modified.
 *  <p>
 *  This dataset passes transactions down to a nominated backing {@link DatasetGraph}.
 *  <p>
 *  It is particular suitable for use in streams where each element is a single named graph.
 */
public class DatasetGraphOneNgImpl
    extends DatasetGraphBaseFind
    implements DatasetGraphOneNg
{
    private final Graph graph;
    private final Node graphName;

    private final PrefixMap prefixes;
    private final DatasetGraph backingDGS;
    private final Transactional txn;
    private final boolean supportsAbort;

    public static DatasetGraphOneNg create(Node graphName, Graph graph) {
        // Find the deepest graph, the one that may be attached to a DatasetGraph.
        Graph graph2 = unwrap(graph);
        if ( graph2 instanceof GraphView ) {
            // This becomes a simple class that passes all transaction operations the
            // underlying dataset and masks the fact here are other graphs in the storage.
            return new DatasetGraphOneNgImpl(graphName, graph, ((GraphView)graph2).getDataset());
        }
        // Didn't find a GraphView so no backing DatasetGraph; work on the graph as given.
        return new DatasetGraphOneNgImpl(graphName, graph);
    }

    private static Graph unwrap(Graph graph) {
        for (;;) {
            if ( graph instanceof InfGraph ) {
                graph = ((InfGraph)graph).getRawGraph();
                continue;
            }
            Graph graph2 = GraphOps.unwrapOne(graph);
            if ( graph2 == graph )
                return graph;
            graph = graph2;
        }
    }

    private DatasetGraphOneNgImpl(Node graphName, Graph graph, DatasetGraph backing) {
        this(graphName, graph, backing, backing, backing.supportsTransactionAbort());
    }

    private DatasetGraphOneNgImpl(Node graphName, Graph graph) {
        // Don't advertise the fact but TxnDataset2Graph tries to provide abort.
        // We can not guarantee it though because a plain, non-TIM,
        // memory graph does not support abort.
        this(graphName, graph, null, new TxnDataset2Graph(graph), false);
    }

    private DatasetGraphOneNgImpl(Node graphName, Graph graph, DatasetGraph backing, Transactional txn, boolean supportsAbort) {
        this.graphName = graphName;
        this.graph = graph;
        this.prefixes = Prefixes.adapt(graph);
        this.txn = txn;
        this.backingDGS = backing;
        this.supportsAbort = supportsAbort;
    }

    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public void commit()                      { txn.commit(); }
    @Override public boolean promote(Promote txnType)   { return txn.promote(txnType); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return supportsAbort; }

    @Override
    public boolean containsGraph(Node graphNode) {
        if ( graphName.equals(graphNode) )
            return true;
        return false;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphZero.instance();
    }

    @Override
    public Graph getUnionGraph() {
        if (Quad.isDefaultGraph(graphName))
            return GraphZero.instance();
        else
            return graph;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        if ( isDefaultGraph(graphNode) )
            return getDefaultGraph();
        if ( Quad.isUnionGraph(graphNode) )
            return getUnionGraph();
        if (graphName.equals(graphNode)) {
            return graph;
        }
        return null;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return Collections.singleton(graphName).iterator();
        // return new NullIterator<>();
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    @Override
    public Node getGraphNode() {
        return graphName;
    }

    @Override
    public long size() {
        return Quad.isDefaultGraph(graphName) ? 0 : 1;
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if ( g.equals(graphName) || (Quad.isDefaultGraph(graphName) && Quad.isDefaultGraph(g)) )
            graph.add(new Triple(s, p, o));
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void add(Quad quad) {
        Node g = quad.getGraph();
        if ( g.equals(graphName) || (Quad.isDefaultGraph(graphName) && Quad.isDefaultGraph(g)) )
            graph.add(quad.asTriple());
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if ( g.equals(graphName) || (Quad.isDefaultGraph(graphName) && Quad.isDefaultGraph(g)) )
            graph.delete(new Triple(s, p, o));
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void delete(Quad quad) {
        Node g = quad.getGraph();
        if ( g.equals(graphName) || (Quad.isDefaultGraph(graphName) && Quad.isDefaultGraph(g)) )
            graph.delete(quad.asTriple());
        else
            unsupportedMethod(this, "add(named graph)");
    }

    @Override
    public void setDefaultGraph(Graph g) {
        unsupportedMethod(this, "setDefaultGraph");
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        unsupportedMethod(this, "addGraph");
    }

    @Override
    public void removeGraph(Node graphName) {
        unsupportedMethod(this, "removeGraph");
    }

    // -- Not needed -- implement find(g,s,p,o) directly.
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(graphName) )
            return G.triples2quads(graphName, graph.find(s, p, o));
        else
            return Iter.nullIterator();
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        if ( g.equals(graphName) && !Quad.isDefaultGraph(graphName) )
            return G.triples2quads(graphName, graph.find(s, p, o));
        else
            return Iter.nullIterator();
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(graphName) )
            return Iter.nullIterator();
        else
            return G.triples2quads(graphName, graph.find(s, p, o));
    }

    protected static boolean isDefaultGraph(Quad quad) {
        return isDefaultGraph(quad.getGraph());
    }

    protected static boolean isDefaultGraph(Node quadGraphNode) {
        return (quadGraphNode == null || Quad.isDefaultGraph(quadGraphNode));
    }

    // It's just easier and more direct ...
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        if ( isWildcard(g) || g.equals(graphName) || ( Quad.isDefaultGraph(g) && Quad.isDefaultGraph(graphName)) )
            return G.triples2quads(graphName, graph.find(s, p, o));
        else
            return new NullIterator<>();
    }

    @Override
    public void close() {
        graph.close();
        super.close();
    }
}