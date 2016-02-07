package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.apache.jena.atlas.lib.Sink;

import org.apache.jena.sparql.core.Quad;

public class DatasetListenerSink
    implements DatasetListener
{
    private Sink<Diff<? extends Iterable<Quad>>> sink;

    public DatasetListenerSink(Sink<Diff<? extends Iterable<Quad>>> sink) {
        this.sink = sink;
    }

    @Override
    public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
        sink.send(diff);
    }
}