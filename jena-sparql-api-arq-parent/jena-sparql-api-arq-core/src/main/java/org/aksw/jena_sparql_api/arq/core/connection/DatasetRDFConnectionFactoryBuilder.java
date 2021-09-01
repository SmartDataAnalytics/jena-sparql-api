package org.aksw.jena_sparql_api.arq.core.connection;

import java.util.Objects;

import org.aksw.jena_sparql_api.arq.core.query.QueryEngineFactoryProvider;
import org.aksw.jena_sparql_api.arq.core.update.UpdateEngineFactoryProvider;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;

/**
 * Similar to {@link RDFConnectionFactory#connect(Dataset)} but
 * with additional configuration options to set a {@link Context} and
 * the update / query engines.
 *
 * The result of the builder is of type {@link DatasetRDFConnectionFactory}
 * which is a function from {@link Dataset} to {@link RDFConnection}.
 *
 * @author Claus Stadler
 *
 */
public class DatasetRDFConnectionFactoryBuilder {
    protected QueryEngineFactoryProvider queryEngineFactoryProvider = null;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider = null;

    // Use a context supplier?
    protected Context context;

    public static DatasetRDFConnectionFactoryBuilder create() {
        return new DatasetRDFConnectionFactoryBuilder();
    }

    public DatasetRDFConnectionFactoryBuilder setQueryEngineFactoryProvider(QueryEngineFactoryProvider queryEngineFactoryProvider) {
        this.queryEngineFactoryProvider = queryEngineFactoryProvider;
        return this;
    }

    /** Set the provider to always provide the given query engine factory */
    public DatasetRDFConnectionFactoryBuilder setQueryEngineFactoryProvider(QueryEngineFactory queryEngineFactory) {
        this.queryEngineFactoryProvider = (query, dataset, context) -> queryEngineFactory;
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setDefaultQueryEngineFactoryProvider() {
        this.queryEngineFactoryProvider = QueryEngineRegistry::findFactory;
        return this;
    }


    public DatasetRDFConnectionFactoryBuilder setUpdateEngineFactoryProvider(UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
        return this;
    }

    /** Set the provider to always provide the given query engine factory */
    public DatasetRDFConnectionFactoryBuilder setUpdateEngineFactoryProvider(UpdateEngineFactory updateEngineFactory) {
        this.updateEngineFactoryProvider = (dataset, context) -> updateEngineFactory;
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setDefaultUpdateEngineFactoryProvider() {
        this.updateEngineFactoryProvider = (dataset, context) -> UpdateEngineRegistry.findFactory(dataset, context);
        return this;
    }

    public DatasetRDFConnectionFactoryBuilder setContext(Context context) {
        this.context = context;
        return this;
    }

    public DatasetRDFConnectionFactory build() {
        // The purpose of using this builder is to setup the providers
        // If they are null we assume a mistake
        Objects.requireNonNull(queryEngineFactoryProvider);
        Objects.requireNonNull(updateEngineFactoryProvider);

        return new DatasetRDFConnectionFactoryImpl(context, queryEngineFactoryProvider, updateEngineFactoryProvider);
    }

}
