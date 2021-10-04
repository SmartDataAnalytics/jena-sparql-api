package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.apache.jena.graph.Node;

/**
 * A ConceptManager allocates names for concepts and roles.
 * Furthermore, it may support subsumption checking on a best-effort basis.
 *
 *
 * @author raven
 *
 */
public interface ConceptManager {
    Node getOrCreate(UnaryRelation concept);
}
