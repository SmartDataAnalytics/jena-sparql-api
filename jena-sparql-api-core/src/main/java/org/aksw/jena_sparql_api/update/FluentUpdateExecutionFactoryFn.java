package org.aksw.jena_sparql_api.update;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.FluentFnBase;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDatasetDescription;
import org.aksw.jena_sparql_api.parse.UpdateExecutionFactoryParse;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.update.UpdateRequest;


public class FluentUpdateExecutionFactoryFn<P>
    extends FluentFnBase<UpdateExecutionFactory, P>
{

    public FluentUpdateExecutionFactoryFn<P> withDatasetDescription(final String withIri, final DatasetDescription datasetDescription) {
        compose(new Function<UpdateExecutionFactory, UpdateExecutionFactory>() {
            @Override
            public UpdateExecutionFactory apply(UpdateExecutionFactory uef) {
                UpdateExecutionFactory r = new UpdateExecutionFactoryDatasetDescription(uef, withIri, datasetDescription);
                return r;
            }
        });
        return this;
    }

    public FluentUpdateExecutionFactoryFn<P> withParser(final Function<String, UpdateRequest> parser) {
        compose(new Function<UpdateExecutionFactory, UpdateExecutionFactory>() {
            @Override
            public UpdateExecutionFactory apply(UpdateExecutionFactory uef) {
                UpdateExecutionFactory r = new UpdateExecutionFactoryParse(uef, parser);
                return r;
            }
        });

        return this;
    }

}
