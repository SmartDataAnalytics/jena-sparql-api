package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

/**
 * Paginator based on a concept.
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public abstract class ListPaginatorSparqlQueryBase<T>
    implements ListPaginator<T>
{
    protected QueryExecutionFactory qef;
    protected Concept filterConcept;
    protected boolean isLeftJoin;

    public ListPaginatorSparqlQueryBase(QueryExecutionFactory qef, Concept filterConcept, boolean isLeftJoin) {
        this.qef = qef;
        this.filterConcept = filterConcept;
        this.isLeftJoin = isLeftJoin;
    }
}
