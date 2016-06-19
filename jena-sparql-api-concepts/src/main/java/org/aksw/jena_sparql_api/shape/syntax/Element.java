package org.aksw.jena_sparql_api.shape.syntax;

/**
 * Element for building a concept
 *
 * Full name would be ConceptElement
 * @author raven
 *
 */
public interface Element {
    <T> T accept(ElementVisitor<T> visitor);
}
