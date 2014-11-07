package org.aksw.jena_sparql_api.concept_cache;

import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionDecoratorBase;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class QueryExecutionConceptCache
    extends QueryExecutionDecoratorBase<QueryExecution>
{
    private ConceptMap conceptMap;
    private Query query;
    private Set<Var> indexVars;

    public QueryExecutionConceptCache(QueryExecution decoratee, ConceptMap conceptMap, Query query, Set<Var> indexVars) {
        super(decoratee);

        this.conceptMap = conceptMap;
        this.query = query;
        this.indexVars = indexVars;
    }

    @Override
    public ResultSet execSelect() {

        ResultSet rs = decoratee.execSelect();
        rs = ResultSetFactory.copyResults(rs);

        ResultSet cacheRs = ResultSetUtils.project(rs, indexVars, true);
        conceptMap.index(query, cacheRs);

        return rs;
    }
}