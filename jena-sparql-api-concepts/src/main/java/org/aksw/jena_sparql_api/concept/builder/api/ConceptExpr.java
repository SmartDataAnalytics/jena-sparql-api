package org.aksw.jena_sparql_api.concept.builder.api;

import org.aksw.jena_sparql_api.concepts.Concept;

public interface ConceptExpr
{
    public static final String BUILDER = "builder";
    public static final String CONCEPT = "concept";
    public static final String LIST = "list";
    public static final String UNKNOWN = "unknown";

    Object getType();

    boolean isConcept();
    boolean isBuilder();
    boolean isList();

    Concept asConcept(); // TODO Maybe only cast as sub type rather than reaching into the object
    ConceptBuilder asBuilder(); // TODO Maybe only cast as sub type rather than reaching into the object
    ConceptExprList asList();


    /**
     * Custom extension point
     *
     * @return
     */
    Object asObject();

    <T> T accept(ConceptExprVisitor<T> visitor);
}
