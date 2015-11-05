package org.aksw.jena_sparql_api.update;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.changeset.ChangeSet;
import org.aksw.jena_sparql_api.changeset.ChangeSetMetadata;
import org.aksw.jena_sparql_api.changeset.ChangeSetUtils;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Quad;

public class DatasetListenerTrack
    implements DatasetListener
{
    //private Sink<Diff<? extends Iterable<Quad>>> sink;
    //private UpdateExecutionFactory uef;
    private SparqlService trackerService;
    private ChangeSetMetadata changesetMetadata;
    private String prefix;

    public DatasetListenerTrack(SparqlService trackerService) {
        this.trackerService = trackerService;
    }

    @Override
    public void onPreModify(Diff<Set<Quad>> quadDiff, UpdateContext updateContext) {

        SparqlService sparqlService = updateContext.getSparqlService();
        SparqlServiceReference ssr = new SparqlServiceReference(sparqlService.getServiceUri(), sparqlService.getDatasetDescription());


        Map<Node, Diff<Set<Triple>>> tripleDiff = DiffQuadUtils.partitionQuads(quadDiff);

        for(Entry<Node, Diff<Set<Triple>>> entry : tripleDiff.entrySet()) {
            Node g = entry.getKey();
            Diff<Set<Triple>> diff = entry.getValue();

            QueryExecutionFactory qef = trackerService.getQueryExecutionFactory();
            Map<Node, ChangeSet> changesets = ChangeSetUtils.createChangeSets(qef, changesetMetadata, diff, prefix);

            for(ChangeSet changeset : changesets.values()) {
                Model model = ModelFactory.createDefaultModel();
                ChangeSetUtils.write(model, changeset);

                ChangeSetUtils.enrichWithSource(model, g, ssr);

            }


        }

        //sink.send(diff);
    }
}