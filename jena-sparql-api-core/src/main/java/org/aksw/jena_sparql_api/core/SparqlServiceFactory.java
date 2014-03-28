package org.aksw.jena_sparql_api.core;

import java.util.Collection;

public interface SparqlServiceFactory {
    QueryExecutionFactory createSparqlService(String serviceUri, Collection<String> defaultGraphUris);
}
