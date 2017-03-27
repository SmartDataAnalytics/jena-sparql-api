package org.aksw.jena_sparql_api.update;

import java.util.function.Function;

import org.aksw.jena_sparql_api.core.FluentFnBase;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceFactoryDefaults;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class FluentSparqlServiceFactoryFn<P>
    extends FluentFnBase<SparqlServiceFactory, P>
{
    public FluentSparqlServiceFactoryFn() {
        super(true);
    }

    public static FluentSparqlServiceFactoryFn<?> start() {
        return new FluentSparqlServiceFactoryFn<Object>();
    }

    /**
     * Set a default service uri that is used if null is passed as the serivceUri
     * argument of SparqlServiceFactory.createSparqlService(null, ....)
     *
     * @param serviceUri
     * @return
     */
    public FluentSparqlServiceFactoryFn<P> defaultServiceUri(final String defaultServiceUri, final Predicate<String> serviceUriValidator) {
        compose(new Function<SparqlServiceFactory, SparqlServiceFactory>() {
            @Override
            public SparqlServiceFactory apply(SparqlServiceFactory ssf) {
                SparqlServiceFactory r = new SparqlServiceFactoryDefaults(ssf, defaultServiceUri, serviceUriValidator);
                return r;
            }
        });
        return this;
    }

    public FluentSparqlServiceFactoryFn<P> defaultServiceUri(String defaultServiceUri) {
        FluentSparqlServiceFactoryFn<P> result = defaultServiceUri(defaultServiceUri, null);
        return result;
    }


    public FluentSparqlServiceFn<FluentSparqlServiceFactoryFn<P>> configService() {
        final FluentSparqlServiceFactoryFn<P> self = this;

        final FluentSparqlServiceFn<FluentSparqlServiceFactoryFn<P>> result = new FluentSparqlServiceFn<FluentSparqlServiceFactoryFn<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlServiceFactoryFn<P>>() {
            @Override
            public FluentSparqlServiceFactoryFn<P> get() {
                // Apply the collection transformations
                final Function<SparqlService, SparqlService> transform = result.value();
                    compose(new Function<SparqlServiceFactory, SparqlServiceFactory>() {
                        @Override
                        public SparqlServiceFactory apply(SparqlServiceFactory sparqlServiceFactory) {
                            SparqlServiceFactory r = new SparqlServiceFactoryTransform(sparqlServiceFactory, transform);
                            return r;
                        }
                    });

                    return self;
                }
        });

        return result;
    }

}
