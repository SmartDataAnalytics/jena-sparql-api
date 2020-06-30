package org.aksw.jena_sparql_api.algebra.transform;

public enum SubstitutionStrategy {
    /**
     * Retain the value - suppress substitution
     */
    RETAIN(false, true),                 // (:g ?s ?p ?o) -> (:g ?s ?p ?o)

    /**
     * Apply substitution
     */
    SUBSTITUTE(true, false),            // (:g ?s ?p ?o) -> (?graph ?s ?p ?o)

    /**
     * Substitute and constrain to the original value
     *
     */
    SUSTITUTE_AND_FILTER(true, true); // (:g ?s ?p ?o) -> (?graph ?s ?p ?o) FILTER(?graph = :g)

    protected boolean substitute;
    protected boolean injectFilter;

    SubstitutionStrategy(boolean substitute, boolean injectFilter) {
        this.substitute = substitute;
        this.injectFilter = injectFilter;
    }

    public boolean isSubstitute() {
        return substitute;
    }

    public boolean isInjectFilter() {
        return injectFilter;
    }
}