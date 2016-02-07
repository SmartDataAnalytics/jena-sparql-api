package org.aksw.jena_sparql_api.changeset;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.apache.jena.atlas.lib.Sink;

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class SinkChangeSetWriter
    implements Sink<Diff<? extends Iterable<Quad>>>
{
    private ChangeSetMetadata metadata;
    private SparqlService sparqlService;

    public SinkChangeSetWriter(ChangeSetMetadata metadata, SparqlService sparqlService) {
        this.metadata = metadata;
        this.sparqlService = sparqlService;
    }

    @Override
    public void send(Diff<? extends Iterable<Quad>> diff) {
        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

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
