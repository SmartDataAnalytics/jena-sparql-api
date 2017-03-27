package org.aksw.jena_sparql_api.concept_cache.core;

import org.apache.jena.sparql.algebra.Op;

/**
 * Interface for things having an Op attribute.
 * Intended use is with Contextual:
 *
 * contextual.unwrap(OpAttribute, true).getOp();
 *
 * @author raven
 *
 */
public interface OpAttribute {
	Op getOp();
}
