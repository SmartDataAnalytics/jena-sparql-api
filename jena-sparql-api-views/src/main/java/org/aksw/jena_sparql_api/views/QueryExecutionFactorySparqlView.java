package org.aksw.jena_sparql_api.views;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.algebra.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutionFactorySparqlView
    extends QueryExecutionFactoryBackQuery
{
    private static Logger logger = LoggerFactory.getLogger(QueryExecutionFactorySparqlView.class);

    private QueryExecutionFactory factory;
    //private SparqlViewSystem system;
    private CandidateViewSelector<SparqlView> candidateViewSelector;
    private Dialect dialect;

    public QueryExecutionFactorySparqlView(QueryExecutionFactory factory, CandidateViewSelector<SparqlView> candidateViewSelector, Dialect dialect) {
        this.factory = factory;
        this.candidateViewSelector = candidateViewSelector;
        this.dialect = dialect;
    }

    @Override
    public String getId() {
        return factory.getId() + "-" + hashCode();
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        Op rewrittenOp = candidateViewSelector.getApplicableViews(query);

        //rewrittenOp = Optimize.optimize(rewrittenOp, ARQ.getContext());

        Query rewritten = MyOpAsQuery.asQuery(rewrittenOp, dialect);

        System.out.println("Rewritten query: " + rewritten);


        //Query rewritten = SparqlViewSystem.rewrite(query, system, dialect);
        //logger.trace("Rewritten query: " + rewritten);
        //System.out.println("Rewritten query: " + rewritten);
        QueryExecution result = factory.createQueryExecution(rewritten);

        return result;
    }
}