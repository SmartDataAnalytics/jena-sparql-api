package org.aksw.jena_sparql_api.update;

import java.util.Collection;

import org.aksw.jena_sparql_api.core.DatasetListener;
import org.aksw.jena_sparql_api.core.FluentFnBase;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactoryFn;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.SparqlServiceImpl;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactory;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.core.DatasetDescription;

public class FluentSparqlServiceFn<P>
    extends FluentFnBase<SparqlService, P>
{
    public FluentQueryExecutionFactoryFn<FluentSparqlServiceFn<P>> configQuery() {
        final FluentSparqlServiceFn<P> self = this;
        final FluentQueryExecutionFactoryFn<FluentSparqlServiceFn<P>> result = new FluentQueryExecutionFactoryFn<FluentSparqlServiceFn<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlServiceFn<P>>() {
            @Override
            public FluentSparqlServiceFn<P> get() {
                final Function<QueryExecutionFactory, QueryExecutionFactory> fn = result.value();

                compose(new Function<SparqlService, SparqlService>() {
                    @Override
                    public SparqlService apply(SparqlService sparqlService) {
                        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
                        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

                        qef = fn.apply(qef);

                        String serviceUri = sparqlService.getServiceUri();
                        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
                        SparqlService r = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
                        return r;
                    }
                });

                return self;
            }
        });

        return result;
    }


    public FluentUpdateExecutionFactoryFn<FluentSparqlServiceFn<P>> configUpdate() {
        final FluentSparqlServiceFn<P> self = this;
        final FluentUpdateExecutionFactoryFn<FluentSparqlServiceFn<P>> result = new FluentUpdateExecutionFactoryFn<FluentSparqlServiceFn<P>>();
        result.setParentSupplier(new Supplier<FluentSparqlServiceFn<P>>() {
            @Override
            public FluentSparqlServiceFn<P> get() {
                final Function<UpdateExecutionFactory, UpdateExecutionFactory> fn = result.value();

                compose(new Function<SparqlService, SparqlService>() {
                    @Override
                    public SparqlService apply(SparqlService sparqlService) {
                        QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
                        UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();

                        uef = fn.apply(uef);

                        String serviceUri = sparqlService.getServiceUri();
                        DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
                        SparqlService r = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
                        return r;
                    }
                });

                return self;
            }
        });

        return result;
    }

    public <T extends UpdateExecutionFactory & DatasetListenable> FluentSparqlServiceFn<P> withUpdateListeners(final Function<SparqlService, T> updateStrategy, final Collection<DatasetListener> listeners) {

        compose(new Function<SparqlService, SparqlService>() {
            @Override
            public SparqlService apply(SparqlService sparqlService) {
                QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
                T uef = updateStrategy.apply(sparqlService);
                uef.getDatasetListeners().addAll(listeners);
                //UpdateContext updateContext = new UpdateContext(sparqlService, batchSize, containmentChecker);

                //UpdateExecutionFactory uef = new UpdateExecutionFactoryEventSource(updateContext);
                String serviceUri = sparqlService.getServiceUri();
                DatasetDescription datasetDescription = sparqlService.getDatasetDescription();
                SparqlService r = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
                return r;
            }
        });

        return this;
    }

//    public FluentSparqlServiceFn<P> withParser(final Syntax syntax, final Prologue prologue) {
//        compose(new Function<SparqlService, SparqlService>() {
//            @Override
//            public SparqlService apply(SparqlService sparqlService) {
//                QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//                qef = new QueryExecutionFactoryDatasetDescription(qef, datasetDescription);
//
//                UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
//                uef = new UpdateExecutionFactoryDatasetDescription(uef, withIri, datasetDescription);
//
//                String serviceUri = sparqlService.getServiceUri();
//                SparqlService r = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
//                return r;
//            }
//        });
//
//    	return result;
//    }


    public FluentSparqlServiceFn<P> withDatasetDescription(final DatasetDescription datasetDescription) {
        String withIri = DatasetDescriptionUtils.getSingleDefaultGraphUri(datasetDescription);

        if(withIri == null) {
            throw new RuntimeException("Can only derive a withIri if there is exactly one default graph; got: " + DatasetDescriptionUtils.toString(datasetDescription));
        }

        FluentSparqlServiceFn<P> result = withDatasetDescription(datasetDescription, withIri);
        return result;
    }

    public FluentSparqlServiceFn<P> withDatasetDescription(final DatasetDescription datasetDescription, final String withIri) {
        configQuery()
            .withDatasetDescription(datasetDescription)
        .end()
        .configUpdate()
            .withDatasetDescription(withIri, datasetDescription)
        .end()
        .compose(new Function<SparqlService, SparqlService>() {
           @Override
            public SparqlService apply(SparqlService ss) {
               SparqlService r = new SparqlServiceImpl(ss.getServiceUri(), datasetDescription, ss.getQueryExecutionFactory(), ss.getUpdateExecutionFactory());
               return r;
            }
        });
//
//        compose(new Function<SparqlService, SparqlService>() {
//            @Override
//            public SparqlService apply(SparqlService sparqlService) {
//                QueryExecutionFactory qef = sparqlService.getQueryExecutionFactory();
//                qef = new QueryExecutionFactoryDatasetDescription(qef, datasetDescription);
//
//                UpdateExecutionFactory uef = sparqlService.getUpdateExecutionFactory();
//                uef = new UpdateExecutionFactoryDatasetDescription(uef, withIri, datasetDescription);
//
//                String serviceUri = sparqlService.getServiceUri();
//                SparqlService r = new SparqlServiceImpl(serviceUri, datasetDescription, qef, uef);
//                return r;
//            }
//        });
//
        return this;
    }


    public static FluentSparqlServiceFn<?> start() {
        return new FluentSparqlServiceFn<Object>();
    }

}
