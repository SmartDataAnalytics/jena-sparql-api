package org.aksw.jena_sparql_api.path.relgen;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.path.core.PathPE;


/**
 * A supplier that sees all constraints on prior relations plus
 * the 'block' of constraints on its predecessor.
 *
 * @author raven
 *
 */
public interface RelationProvider {
    Relation getRelation(PathPE absPath, PathPE block);
}
