package org.aksw.jena_sparql_api.concept_cache;

import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

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
