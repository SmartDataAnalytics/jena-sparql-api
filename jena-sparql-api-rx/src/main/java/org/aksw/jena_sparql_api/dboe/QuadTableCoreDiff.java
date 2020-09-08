package org.aksw.jena_sparql_api.dboe;

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * A diff based StorageRDF similar to the Delta graph
 *
 * @author raven
 *
 */
public class QuadTableCoreDiff
    implements QuadTableCore
{
    protected QuadTableCore master;
    protected QuadTableCore additions;
    protected QuadTableCore deletions;

    public void clearDiff() {
        additions.clear();
        deletions.clear();
    }

    public void applyDiff() {
        applyDiff(master, additions, deletions);
        clearDiff();
    }

    public static void applyDiff(QuadTableCore target, QuadTableCore additions, QuadTableCore deletions) {
        deletions.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).forEach(target::delete);
        additions.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).forEach(target::add);
    }

    public QuadTableCoreDiff(QuadTableCore master, QuadTableCore additions, QuadTableCore deletions) {
        this.master = master;
        this.additions = additions;
        this.deletions = deletions;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Invocation of .clear() on a diff is not supported; you may use .clearDiff()");
    }

    @Override
    public void add(Quad quad) {
        additions.add(quad);
        deletions.delete(quad);
    }

    @Override
    public void delete(Quad quad) {
        deletions.add(quad);
        additions.delete(quad);
    }

    @Override
    public Stream<Quad> find(Node g, Node s, Node p, Node o) {
        return Stream.concat(
                master.find(g, s, p, o).filter(q -> !deletions.contains(q)),
                additions.find(g, s, p, o).filter(q -> !master.contains(q)));
    }

    @Override
    public Stream<Node> listGraphNodes() {
        return
            Stream.concat(master.listGraphNodes(), additions.listGraphNodes()).distinct()
                .filter(g -> {
                    boolean r = true; // may become false
                    boolean hasDeletionsInG = deletions.find(g, Node.ANY, Node.ANY, Node.ANY).findAny().isPresent();
                    if (hasDeletionsInG) {
                        // For graph g test if there is any triple in master+additions that is not in deletions
                        r = Stream.concat(
                                master.find(g, Node.ANY, Node.ANY, Node.ANY),
                                additions.find(g, Node.ANY, Node.ANY, Node.ANY))
                            .filter(q -> !deletions.contains(q))
                            .findAny().isPresent();
                    }
                    return r;
                });
    }

}