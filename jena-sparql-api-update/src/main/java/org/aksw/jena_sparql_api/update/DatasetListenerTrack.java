package org.aksw.jena_sparql_api.update;

import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;

import com.hp.hpl.jena.sparql.core.Quad;

public class DatasetListenerTrack
    implements DatasetListener
{
    //private Sink<Diff<? extends Iterable<Quad>>> sink;
    //private UpdateExecutionFactory uef;
    private SparqlService trackerService;

    public DatasetListenerTrack(SparqlService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public void onPreModify(Diff<Set<Quad>> diff, SparqlServiceReference serviceReference, UpdateContext updateContext) {
        //sink.send(diff);
    }
}