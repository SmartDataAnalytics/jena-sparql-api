package org.aksw.difs.engine;

import org.aksw.jena_sparql_api.arq.core.connection.DatasetRDFConnectionFactory;
import org.aksw.jena_sparql_api.arq.core.connection.DatasetRDFConnectionFactoryBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.util.Context;

public class RDFConnectionFactoryQuadForm {

    /** Connect to a dataset using the quad form engine */
    public static RDFConnection connect(Dataset dataset) {
        return connect(dataset, null);
    }

    public static DatasetRDFConnectionFactory createFactory(Context context) {
        return DatasetRDFConnectionFactoryBuilder.create()
            .setQueryEngineFactoryProvider(QueryEngineQuadForm.FACTORY)
            .setUpdateEngineFactoryProvider(UpdateEngineMainQuadForm.FACTORY)
            .setContext(context)
            .build();
    }

    public static RDFConnection connect(Dataset dataset, Context context) {
        RDFConnection result = createFactory(context)
                .apply(dataset);

        return result;
    }

}
