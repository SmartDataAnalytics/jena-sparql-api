package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.google.common.base.Stopwatch;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

public class QueryRunner {
    private QueryExecutionFactory sparqlService;
    private PrefixMapping prefixMapping;
    private Syntax syntax;

    public QueryRunner(QueryExecutionFactory sparqlService) {
        this(sparqlService, new PrefixMappingImpl());
    }

    public QueryRunner(QueryExecutionFactory sparqlService, PrefixMapping prefixMapping) {
        this(sparqlService, prefixMapping, Syntax.syntaxARQ);
    }

    public QueryRunner(QueryExecutionFactory sparqlService, PrefixMapping prefixMapping, Syntax syntax) {
        this.sparqlService = sparqlService;
        this.prefixMapping = prefixMapping;
        this.syntax = syntax;
    }

    public QueryRunner trySelect(String queryString) {
        Stopwatch sw = Stopwatch.createStarted();


        Query query = new Query();
        query.setPrefixMapping(prefixMapping);

        QueryFactory.parse(query, queryString, "http://example.org/", syntax);

        QueryExecution qe = sparqlService.createQueryExecution(query);
        ResultSet rs = qe.execSelect();
        ResultSetFormatter.consume(rs);


        System.out.println("Time taken: " + sw.elapsed(TimeUnit.MILLISECONDS));

        return this;
    }
}
