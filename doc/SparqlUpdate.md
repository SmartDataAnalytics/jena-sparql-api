## SPARQL Update

Jena Sparql Api offers the following SPARQL Update features:

* Core Interfaces for performing SPARQL Update requests together with basic implementations for HTTP and Model backends.
* Advanced implementions that emit events about the triples (quads) being changed by intercepting and transforming original update requests.
* ChangeSet support for tracking changes in a remote graph

The core interface for creating update execution requests is `org.aksw.jena_sparql_api.core.UpdateExecutionFactory`.

### Architecture
The following concepts are crucial for understanding the jena-sparql-api SPARQL Update system:
* SparqlService: An object providing SPARQL-based read and write functionality via `getQueryExecutionFactory` and `getUpdateExecutionFactory`.
* UpdateStrategy: An `UpdateStrategy` is simply a `Function` that given a `SparqlService` returns a `UpdateExecutionFactory`. This is *not necessarily* the `SparqlService`'s native UpdateExecutionFactory.
 * UpdateStrategyEventSource: An `UpdateStrategy` whose created `UpdateExecutionFactory` support listening for changes to data.
  * QuadContainmentChecker:  Interface for checking whether a set of quads is present in the data accessible via a `QueryExecutionFactory`.
This is needed in order for the `UpdateStrategyEventSource` to only emits events when there are actual changes.
Conversely, if there is a request to insert an already existing triple / remove a non-existing triple, no event will be raised.
* DatasetListener: An interface with the method `.onPreModify(...)` on which changes are announced using a `Diff` object.
* Diff<Set<Quad>>: An object representing a set of added and removed quads.


### Simple Example for Intercepting events
The magic in this example comes from the `UpdateStrategyEventSource`:
An `UpdateStrategy` is simply a `Function` that given a `SparqlService` returns a `UpdateExecutionFactory`.
In the simplest case, this function could just return the service's native UpdateExecutionFactory (i.e. `sparqlService.getUpdateExecutionFactory()`).
In the example below, we use an `UpdateStrategyEventSource` that enables us to listen to the changes being performed on the SparqlService.
This update strategy decorates the service's native update execution factory with `UpdateExecutionFactoryEventSource`.

Note: Currently the updateExecution does not invalidate query caches. However, this is planned in the future.


```java
package org.aksw.jena_sparql_api.update;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.core.SparqlService;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.update.UpdateRequest;

class MainSparqlUpdateSimpleDemo {

    public static void demoWithModel() throws Exception
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
        SparqlService sparqlService = FluentSparqlServiceFactory
            .forModel()
            .withUpdateListeners(new UpdateStrategyEventSource(), listeners)
            .create();

        // Perform the request - the listeners will be notified appropriately
        UpdateRequest updateRequest = UpdateUtils.parse("Prefix ex: <http://example.org/> Insert Data { ex:s ex:p ex:o }");
        sparqlService
            .getUpdateExecutionFactory()
            .createUpdateProcessor(updateRequest)
            .execute();
    }

    public static void demoWithRemoteHttpAccess() throws Exception
        List<DatasetListener> listeners = Collections.<DatasetListener>singletonList(new DatasetListener() {
            @Override
            public void onPreModify(Diff<Set<Quad>> diff, UpdateContext updateContext) {
                // Print out any changes to the console
                System.out.println(diff);
            }
        });

        HttpAuthenticator auth = new SimpleAuthenticator("dba", "dba".toCharArray());

        SparqlService sparqlService = FluentSparqlService
            .http("http://your.write.enabled/sparql-endpoint", "http://dpbedia.org", auth)
            .config()
                .configQuery()
                    .withPagination(100)
                .end()
                .withUpdateListeners(new UpdateStrategyEventSource(), listeners)
            .end()
            .create();

        UpdateRequest updateRequest = UpdateUtils.parse("Prefix ex: <http://example.org/> Insert { ex:s ex:p ex:o }");
        sparqlService
            .getUpdateExecutionFactory()
            .createUpdateProcessor(updateRequest)
            .execute();


}

```







