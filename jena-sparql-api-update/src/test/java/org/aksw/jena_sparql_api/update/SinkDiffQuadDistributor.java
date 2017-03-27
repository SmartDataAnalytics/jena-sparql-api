package org.aksw.jena_sparql_api.update;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.apache.jena.atlas.lib.Sink;

import com.google.common.base.Function;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

/**
 * Partitions a diff into several diffs based on the graph and distributes
 * it to other sinks
 *
 * @author raven
 *
 */
public class SinkDiffQuadDistributor
    implements Sink<Diff<? extends Iterable<Quad>>>
{

    private Function<String, Sink<Diff<? extends Iterable<Quad>>>> graphToSink;

    @Override
    public void send(Diff<? extends Iterable<Quad>> item) {
        Map<Node, Diff<Set<Quad>>> graphToDiff = DiffQuadUtils.partitionQuadsByGraph(item);

        for(Entry<Node, Diff<Set<Quad>>> entry : graphToDiff.entrySet()) {
            String graphName = entry.getKey().toString();

            Sink<Diff<? extends Iterable<Quad>>> target = graphToSink.apply(graphName);

            target.send(item);

            target.flush();
            target.close();
        }

    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }
}