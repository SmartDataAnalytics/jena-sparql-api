package org.aksw.jena_sparql_api.update;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.FluentBase;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.apache.http.client.HttpClient;
import org.apache.jena.sparql.core.DatasetDescription;

import com.google.common.base.Supplier;


public class FluentSparqlServiceFactory<P>
    extends FluentBase<SparqlServiceFactory, P>
{
    public FluentSparqlServiceFactory(SparqlServiceFactory sparqlServiceFactory) {
        this.fn = sparqlServiceFactory;
    }

    public FluentSparqlServiceFactoryFn<FluentSparqlServiceFactory<P>> configFactory() {
        final FluentSparqlServiceFactory<P> self = this;

        final FluentSparqlServiceFactoryFn<FluentSparqlServiceFactory<P>> result = new FluentSparqlServiceFactoryFn<FluentSparqlServiceFactory<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlServiceFactory<P>>() {
                @Override
                public FluentSparqlServiceFactory<P> get() {
                    // Apply the collection transformations
                    Function<SparqlServiceFactory, SparqlServiceFactory> transform = result.value();
                    fn = transform.apply(fn);

                    return self;
                }
            });

        return result;
    }


//    public FluentSparqlServiceFn<FluentSparqlServiceFactory<P>> configService() {
//        FluentSparqlServiceFn<FluentSparqlServiceFactory<P>> result = config();
//        return result;
//    }

    /**
     * Use configService instead
     *
     * @return
     */
    @Deprecated
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
                                HttpClient httpClient) {
                            // TODO Auto-generated method stub
                            SparqlService raw = current.createSparqlService(serviceUri, datasetDescription, httpClient);
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
