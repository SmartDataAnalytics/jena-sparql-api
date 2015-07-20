package org.aksw.jena_sparql_api.core;

/**
 * A SparqlService is an object that bundles together related sparql features
 * - i.e. querying and updating.
 *
 *
 * @author raven
 *
 */
public interface SparqlService {
    QueryExecutionFactory getQueryExecutionFactory();
    UpdateExecutionFactory getUpdateExecutionFactory();
}
