package org.aksw.jena_sparql_api.algebra.transform;

/**
 * Substitution strategy for constants in SPARQL graph patterns.
 * One of:
 * <ul>
 * <li>RETAIN => do nothing</li>
 * <li>SUBSTITUTE => substitute affected constant with a variable, but do NOT inject a filter to the constant</li>
 * <li>SUSTITUTE_AND_FILTER => substitute affected constant with a variablbe and inject a filter statement
 *   that constraints the variable to the original value</li>
 * </ul>
 *
 * @author raven
 *
 */
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