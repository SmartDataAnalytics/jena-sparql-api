package org.aksw.jena_sparql_api.concept_cache.core;

import java.util.Set;

import org.aksw.jena_sparql_api.concept_cache.dirty.SparqlViewCache;
import org.aksw.jena_sparql_api.concept_cache.domain.ProjectedQuadFilterPattern;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;


/**
 * TODO Class not finished
 *
 * Iterator, that tracks iterated items in memory, and once its end or
 * a threshold on items was reached, passes the read data on.
 *
 *
 *
 * @author raven
 *
 */
public class BindingIteratorCaching
    extends ResultSetCloseable
{
    protected ResultSet physicalRs;
    protected Set<Var> indexVars;
    protected long indexResultSetSizeThreshold;
    protected SparqlViewCache sparqlViewCache;
    protected ProjectedQuadFilterPattern pqfp;

    public BindingIteratorCaching(ResultSet decoratee, ResultSet physicalRs,
            Set<Var> indexVars, long indexResultSetSizeThreshold,
            SparqlViewCache sparqlViewCache, ProjectedQuadFilterPattern pqfp) {
        super(decoratee);
        this.physicalRs = physicalRs;
        this.indexVars = indexVars;
        this.indexResultSetSizeThreshold = indexResultSetSizeThreshold;
        this.sparqlViewCache = sparqlViewCache;
        this.pqfp = pqfp;
    }



}
