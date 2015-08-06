package org.aksw.jena_sparql_api.batch.processor;

import org.aksw.jena_sparql_api.concepts.Concept;

import com.hp.hpl.jena.update.UpdateProcessor;

/**
 * Perform a pre-configured update with a set of resources
 * @author raven
 *
 */
public interface UpdateService {
    UpdateProcessor prepare(Concept filter);
}