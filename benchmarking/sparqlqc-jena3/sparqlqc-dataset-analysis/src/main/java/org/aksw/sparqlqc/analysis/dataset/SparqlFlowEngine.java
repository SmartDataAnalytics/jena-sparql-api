package org.aksw.sparqlqc.analysis.dataset;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParser;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.Query;

public class SparqlFlowEngine {
    //protected Function<String, Query> queryParser;
    protected SparqlQueryParser queryParser;
    protected QueryExecutionFactory qef;

    public SparqlFlowEngine(QueryExecutionFactory qef) {
        this.qef = qef;
        queryParser = SparqlQueryParserImpl.create();
    }

    public PaginatorQuery fromSelect(String queryStr) {
        Query query = queryParser.apply(queryStr);
        PaginatorQuery result = fromSelect(query);
        return result;
    }

    public PaginatorQuery fromSelect(Query query) {
        PaginatorQuery result = new PaginatorQuery(qef, query);
        return result;
    }

}
