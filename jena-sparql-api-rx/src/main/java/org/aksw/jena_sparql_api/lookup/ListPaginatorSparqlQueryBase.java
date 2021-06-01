package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

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
    protected SparqlQueryConnection qef;
    protected Concept filterConcept;
    protected boolean isLeftJoin;

    public ListPaginatorSparqlQueryBase(SparqlQueryConnection qef, Concept filterConcept, boolean isLeftJoin) {
        this.qef = qef;
        this.filterConcept = filterConcept;
        this.isLeftJoin = isLeftJoin;
    }
}
