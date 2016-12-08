package org.aksw.jena_sparql_api.concept.builder.api;

/**
 * The projection builder is used to specify which attributes to fetch.
 * It always specifies micro-graphs of which one resource acts as the root resource.
 *
 * The motivation is, that rather constructing a table with semantically meaningless rows,
 * we rather yield resources with semantic predicates
 *
 * Note, that multiple result resources can be returned by crafting a fake resource (possibly blank node)
 * for each row of the underyling sparql result set.
 *
 *
 * @author raven
 *
 */
public interface ProjectionBuilder {

}
