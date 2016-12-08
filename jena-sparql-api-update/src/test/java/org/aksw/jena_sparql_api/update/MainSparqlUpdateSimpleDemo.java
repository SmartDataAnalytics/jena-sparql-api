package org.aksw.jena_sparql_api.update;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.UpdateContext;
import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateRequest;

class MainSparqlUpdateSimpleDemo {

    public static void main(String[] args) throws Exception
    {
        // Define the listeners
        List<DatasetListener> listeners = Collections.<DatasetListener>singletonList(new DatasetListener() {
            @Override
            public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
                // Print out any changes to the console
                System.out.println(diff);
            }
        });

        // The fluent API offers convenient construction of common configurations
        // However, should you need more flexibility, you can always create a custom SparqlService decorators.
        SparqlService sparqlService = FluentSparqlService
            .forModel()
            .config()
                .withUpdateListeners(new UpdateStrategyEventSource(), listeners)
            .end()
            .create();

        // Perform the request - the listeners will be notified appropriately
        UpdateRequest updateRequest = UpdateRequestUtils.parse("Prefix ex: <http://example.org/> Insert Data { ex:s ex:p ex:o }");
        sparqlService
            .getUpdateExecutionFactory()
            .createUpdateProcessor(updateRequest)
            .execute();
    }
}
