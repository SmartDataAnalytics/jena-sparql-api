package org.aksw.jena_sparql_api.concept.builder.api;

/**
 * Extension point for custom concept builders
 *
 * @author raven
 *
 */
public interface ConceptBuilderExt
    extends ConceptBuilder
{
    @Override
    default <T> T accept(ConceptBuilderVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
