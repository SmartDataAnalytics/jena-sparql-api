package org.aksw.jena_sparql_api.rx;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.dboe.QuadTableFromNestedMaps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.sparql.core.DatasetGraphQuads;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.mem.QuadTable;
import org.apache.jena.system.Txn;

public class DatasetGraphQuadsImpl
    extends DatasetGraphQuads
{
    protected QuadTable table;
    protected PrefixMap prefixes = PrefixMapFactory.create();
    

    public DatasetGraphQuadsImpl() {
        this(new QuadTableFromNestedMaps());
    }

    public DatasetGraphQuadsImpl(QuadTable table) {
        super();
        this.table = table;
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public void begin(TxnType type) {
        table.begin(TxnType.convert(type));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        table.begin(readWrite);
    }

    @Override
    public boolean promote(Promote mode) {
        return false;
    }

    @Override
    public void commit() {
        table.commit();
    }

    @Override
    public void abort() {
        table.abort();
    }

    @Override
    public void end() {
        table.end();
    }

    @Override
    public ReadWrite transactionMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TxnType transactionType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInTransaction() {
        boolean result = ((Transactional)table).isInTransaction();
        return result;
    }

    private void access(final Runnable source) {
        if (isInTransaction()) {
            source.run();
        } else {
            Txn.executeRead(this, source::run);
        }
    }

    private <T> T access(final Supplier<T> source) {
        return isInTransaction() ? source.get() : Txn.calculateRead(this, source::get);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> result = access(() -> table.find(g, s, p, o).iterator());
        return result;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {

        Node gm = g == null || Quad.isUnionGraph(g) ? Node.ANY : g;

        Iterator<Quad> result;
        if(Quad.isDefaultGraph(gm)) {
            result = Collections.emptyIterator();
//		} else if(Quad.isUnionGraph(gm)) {
//			result = GraphOps.unionGraph(this).find(s, p, o).mapWith(t -> new Quad(Quad.unionGraph, t));
        } else {
            result = access(() -> table.find(gm, s, p, o)
                    .filter(q -> !Quad.isDefaultGraph(q.getGraph()))
                    .iterator());

        }

        return result;
    }

    @Override
    public void add(Quad quad) {
        access(() -> table.add(quad));
    }

    @Override
    public void delete(Quad quad) {
        access(() -> table.delete(quad));
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        graph.find().forEachRemaining(t -> add(new Quad(graphName, t)));
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return access(() -> table.listGraphNodes().iterator());
    }

    public static DatasetGraphQuadsImpl create(Iterator<Quad> it) {
        DatasetGraphQuadsImpl result = new DatasetGraphQuadsImpl();
        while(it.hasNext()) {
            Quad quad = it.next();
            result.add(quad);
        }
        return result;
    }

    public static DatasetGraphQuadsImpl create(Iterable<Quad> quads) {
        DatasetGraphQuadsImpl result = new DatasetGraphQuadsImpl();
        quads.forEach(result::add);
        return result;
    }

    @Override
    public long size() {
        // Comparing with DatasetFactory.create() it seems the count is just
        // the number of named graphs (excluding the default graph)

        //return table.listGraphNodes().count(); //
        return table.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).count();
    }

	@Override
	public PrefixMap prefixes() {
		return prefixes;
	}
}
