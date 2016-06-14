package org.aksw.jena_sparql_api.concept.builder.api;

/**
 * Execution of this kind of queries always returns (jena) ressources
 * whose .getModel() method has the corresponding micrograph attached.
 *
 * Hence, a query returns a stream (result set) of tree-shaped RDF graphs
 *
 *
 *
 * @author raven
 *
 */
public interface QueryBuilder {
    NodeBuilder getNodeBuilder();
    QueryBuilder setConceptBuilder(ConceptBuilder cb);
    QueryBuilder setProjectionBuilder();

}
