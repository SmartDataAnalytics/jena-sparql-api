package org.aksw.jena_sparql_api.lookup;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

public abstract class PaginatorSparqlQueryBase<K, V>
    implements Paginator<K, V>
{

    protected QueryExecutionFactory qef;
    protected Concept filterConcept;
    protected boolean isLeftJoin;

    public PaginatorSparqlQueryBase(QueryExecutionFactory qef, Concept filterConcept, boolean isLeftJoin) {
        this.qef = qef;
        this.filterConcept = filterConcept;
        this.isLeftJoin = isLeftJoin;
    }
}
