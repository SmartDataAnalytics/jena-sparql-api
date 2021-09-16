package org.aksw.jena_sparql_api.arq.core.connection;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * Core interface to create RDFConnections for Datasets.
 *
 * @author Claus Stadler
 *
 */
@FunctionalInterface
public interface DatasetRDFConnectionFactory
//    extends Function<Dataset, RDFConnection>
{
    RDFConnection connect(Dataset dataset);
}
