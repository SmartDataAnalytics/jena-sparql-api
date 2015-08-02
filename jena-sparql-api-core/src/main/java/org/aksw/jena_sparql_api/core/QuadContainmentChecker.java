package org.aksw.jena_sparql_api.core;

import java.util.Set;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * Interface for checking whether a set of quads is available via a given
 * QueryExecutionFactory
 *
 * TODO This interface does not allow for check strategies that would write
 * data into a separate graph - not sure if we should keep this interface, or define a more generic one instead
 *
 * @author raven
 *
 */
public interface QuadContainmentChecker {
    Set<Quad> contains(QueryExecutionFactory qef, Iterable<Quad> quads);
}
