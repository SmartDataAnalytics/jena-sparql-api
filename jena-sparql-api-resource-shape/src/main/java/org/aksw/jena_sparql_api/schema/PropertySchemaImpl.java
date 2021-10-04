package org.aksw.jena_sparql_api.schema;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Intensional specification of an RDF graph (a set of triples)
 * based on a predicate and direction.
 *
 * @author raven
 *
 */
public class PropertySchemaImpl
    implements PropertySchema
{
    protected Node predicate;
    protected boolean isForward;

    /** The schema of the values reachable via this property spec */
    protected Set<NodeSchema> targetSchemas;

    public PropertySchemaImpl(Node predicate, boolean isForward) {
        super();
        this.predicate = predicate;
        this.isForward = isForward;
        this.targetSchemas = new HashSet<NodeSchema>();
    }

    public Node getPredicate() {
        return predicate;
    }

    public boolean isForward() {
        return isForward;
    }

    public Set<NodeSchema> getTargetSchemas() {
        return targetSchemas;
    }


    public boolean canMatchTriples() {
        return true;
    }

    public boolean matchesTriple(Node source, Triple triple) {
        Triple matcher = TripleUtils.create(source, predicate, Vars.o, isForward);
        boolean result = matcher.matches(triple);
        return result;
    }

    public long copyMatchingValues(Node source, Collection<Node> target, Graph sourceGraph) {
        long result = streamMatchingTriples(source, sourceGraph)
            .map(t -> TripleUtils.getTarget(t, isForward))
            .peek(target::add)
            .count();

        return result;
    }

    /**
     * Return a stream of the triples in sourceGraph that match this
     * predicate schema for the given starting node.
     *
     * @param source
     * @param sourceGraph
     * @return
     */
    public Stream<Triple> streamMatchingTriples(Node source, Graph sourceGraph) {
        Triple matcher = TripleUtils.create(source, predicate, Vars.o, isForward);

        ExtendedIterator<Triple> it = sourceGraph.find(matcher);
        Stream<Triple> result = Streams.stream(it);

        return result;

    }

    /**
     * Copy triples that match the predicate specification from the source graph into
     * the target graph.
     *
     * @param target
     * @param source
     */
    public long copyMatchingTriples(Node source, Graph targetGraph, Graph sourceGraph) {
        long result = streamMatchingTriples(source, sourceGraph)
                .peek(targetGraph::add)
                .count();

        return result;
    }

    @Override
    public Path getPath() {
        return isForward ? new P_Link(predicate) : new P_ReverseLink(predicate);
    }
}