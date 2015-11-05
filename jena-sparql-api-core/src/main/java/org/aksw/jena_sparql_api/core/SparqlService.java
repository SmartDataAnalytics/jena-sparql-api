package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.sparql.core.DatasetDescription;

/**
 * A SparqlService is an object that bundles together related sparql features
 * - i.e. querying and updating.
 *
 *
 * @author raven
 *
 */
public interface SparqlService {
    /**
     * Returns the default dataset description associated with this service.
     * May be null.
     * @return
     */
    String getServiceUri();
    DatasetDescription getDatasetDescription();

    QueryExecutionFactory getQueryExecutionFactory();
    UpdateExecutionFactory getUpdateExecutionFactory();
}
