package org.aksw.jena_sparql_api.changeset;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.update.UpdateExecutionFactory;
import org.apache.jena.atlas.lib.Sink;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public class SinkChangeSetWriter
    implements Sink<Diff<? extends Iterable<Quad>>>
{
    private UpdateExecutionFactory uef;
    private QueryExecutionFactory qef;

    private ChangeSetMetadata metadata;


    public SinkChangeSetWriter(ChangeSetMetadata metadata, UpdateExecutionFactory uef, QueryExecutionFactory qef) {
        this.metadata = metadata;
        this.uef = uef;
        this.qef = qef;
    }

    @Override
    public void send(Diff<? extends Iterable<Quad>> diff) {
        UpdateRequest updateRequest = ChangeSetUtils.createUpdateRequest(metadata, qef, diff, "http://example.org/");
        UpdateProcessor updateProcessor = uef.createUpdateProcessor(updateRequest);
        updateProcessor.execute();
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
