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

    public PaginatorQueryBinding fromSelect(String queryStr) {
        Query query = queryParser.apply(queryStr);
        PaginatorQueryBinding result = fromSelect(query);
        return result;
    }

    public PaginatorQueryBinding fromSelect(Query query) {
        PaginatorQueryBinding result = new PaginatorQueryBinding(qef, query);
        return result;
    }

    public PaginatorQueryTriple fromConstruct(String queryStr) {
        Query query = queryParser.apply(queryStr);
        PaginatorQueryTriple result = fromConstruct(query);
        return result;
    }

    public PaginatorQueryTriple fromConstruct(Query query) {
        PaginatorQueryTriple result = new PaginatorQueryTriple(qef, query);
        return result;
    }
}
