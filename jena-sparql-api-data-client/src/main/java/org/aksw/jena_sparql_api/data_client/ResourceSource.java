package org.aksw.jena_sparql_api.data_client;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

/**
 *
 *
 * @author raven
 *
 */
public class ResourceSource {
    protected QueryExecutionFactory qef;
    protected Supplier<Concept> concept;
}
