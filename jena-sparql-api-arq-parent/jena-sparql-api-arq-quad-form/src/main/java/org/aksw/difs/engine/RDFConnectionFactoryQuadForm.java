package org.aksw.difs.engine;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.aksw.jena_sparql_api.core.UpdateExecutionFactoryDataset;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.core.connection.SparqlUpdateConnectionJsa;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionModular;

public class RDFConnectionFactoryQuadForm {

    /** Connect to a dataset using the quad form engine */
    public static RDFConnection connect(Dataset dataset) {

        RDFConnection result = new RDFConnectionModular(
            new SparqlQueryConnectionJsa(
                    new QueryExecutionFactoryDataset(dataset, null, (qu, da, co) -> QueryEngineQuadForm.factory),
                    dataset),

            new SparqlUpdateConnectionJsa(
                new UpdateExecutionFactoryDataset(dataset, null, UpdateProcessorFactoryQuadForm::create),
                dataset),

            RDFConnectionFactory.connect(dataset)
        );

        return result;
    }

}
