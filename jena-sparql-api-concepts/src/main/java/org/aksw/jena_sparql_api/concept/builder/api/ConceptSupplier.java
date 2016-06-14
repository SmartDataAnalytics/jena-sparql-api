package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public interface ConceptSupplier
{
    public static final String BUILDER = "builder";
    public static final String CONCEPT = "concept";
    public static final String UNKNOWN = "unknown";

    Object getType();

    boolean isConcept();
    boolean isBuilder();

    Concept asConcept();
    ConceptBuilder asBuilder();

    /**
     * Custom extension point
     *
     * @return
     */
    Object asObject();
}
