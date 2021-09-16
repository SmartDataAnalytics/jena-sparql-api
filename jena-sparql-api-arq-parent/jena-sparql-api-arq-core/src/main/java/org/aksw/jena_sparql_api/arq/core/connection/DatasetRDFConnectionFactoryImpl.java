package org.aksw.jena_sparql_api.arq.core.connection;

import org.aksw.jena_sparql_api.arq.core.query.QueryEngineFactoryProvider;
import org.aksw.jena_sparql_api.arq.core.query.QueryExecutionFactoryDataset;
import org.aksw.jena_sparql_api.arq.core.update.UpdateEngineFactoryProvider;
import org.aksw.jena_sparql_api.arq.core.update.UpdateProcessorFactoryDataset;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsaBase;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionJsaBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.sparql.util.Context;

/**
 * The default implementation of {@link DatasetRDFConnectionFactory}.
 * Use {@link DatasetRDFConnectionFactoryBuilder} to construct instances.
 *
 */
public class DatasetRDFConnectionFactoryImpl
    implements DatasetRDFConnectionFactory
{
    protected QueryEngineFactoryProvider queryEngineFactoryProvider;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider;

    // Use a context supplier?
    protected Context context;

    public DatasetRDFConnectionFactoryImpl(
            Context context,
            QueryEngineFactoryProvider queryEngineFactoryProvider,
            UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.context = context;
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
    }

    @Override
    public RDFConnection connect(Dataset dataset) {

        RDFConnection result = new RDFConnectionModular(
            new SparqlQueryConnectionJsaBase<>(
                new QueryExecutionFactoryDataset(dataset, context, queryEngineFactoryProvider),
                dataset),

            new SparqlUpdateConnectionJsaBase<>(
                new UpdateProcessorFactoryDataset(dataset, context, updateEngineFactoryProvider),
                dataset),

            RDFConnectionFactory.connect(dataset)
        );

        return result;
    }

}