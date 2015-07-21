package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.FluentBase;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class FluentSparqlServiceFactory<P>
    extends FluentBase<SparqlServiceFactory, P>
{
    public FluentSparqlServiceFactory(SparqlServiceFactory sparqlServiceFactory) {
        this.fn = sparqlServiceFactory;
    }

    public FluentSparqlServiceFn<FluentSparqlServiceFactory<P>> config() {
        final FluentSparqlServiceFactory<P> self = this;

        final FluentSparqlServiceFn<FluentSparqlServiceFactory<P>> result = new FluentSparqlServiceFn<FluentSparqlServiceFactory<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlServiceFactory<P>>() {
                @Override
                public FluentSparqlServiceFactory<P> get() {
                    final SparqlServiceFactory current = fn;

                    // Apply the collection transformations
                    final Function<SparqlService, SparqlService> transform = result.value();
                    SparqlServiceFactory newFactory = new SparqlServiceFactory() {
                        @Override
                        public SparqlService createSparqlService(
                                String serviceUri,
                                DatasetDescription datasetDescription,
                                Object authenticator) {
                            // TODO Auto-generated method stub
                            SparqlService raw = current.createSparqlService(serviceUri, datasetDescription, authenticator);
                            SparqlService r = transform.apply(raw);
                            return r;
                        }
                    };

                    fn = newFactory;

                    return self;
                }
            });

        return result;
    }

    public static FluentSparqlServiceFactory<?> from(SparqlServiceFactory sparqlServiceFactory) {
        FluentSparqlServiceFactory<?> result = new FluentSparqlServiceFactory<Object>(sparqlServiceFactory);
        return result;
    }

}
