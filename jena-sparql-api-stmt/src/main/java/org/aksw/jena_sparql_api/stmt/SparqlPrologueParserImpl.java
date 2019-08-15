package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Prologue;

public class SparqlPrologueParserImpl
    implements SparqlPrologueParser
{
    protected Function<String, Query> sparqlQueryParser;

    public SparqlPrologueParserImpl(Function<String, Query> sparqlQueryParser) {
        super();
        this.sparqlQueryParser = sparqlQueryParser;
    }

    @Override
    public Prologue apply(String prologue) {
        String queryStr = prologue + "SELECT * { ?s ?p ?o }";
        Query query = sparqlQueryParser.apply(queryStr);
        Prologue result = query.getPrologue();
        return result;
    }

}
