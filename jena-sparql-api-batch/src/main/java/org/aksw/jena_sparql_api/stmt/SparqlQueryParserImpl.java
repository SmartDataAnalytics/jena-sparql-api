package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;

public class SparqlQueryParserImpl
    implements SparqlQueryParser
{
    protected Supplier<Query> querySupplier;
    protected Syntax syntax;
    protected String baseURI;


    public SparqlQueryParserImpl(Supplier<Query> querySupplier, Syntax syntax, String baseURI) {
        super();
        this.querySupplier = querySupplier;
        this.syntax = syntax;
        this.baseURI = baseURI;
    }

    @Override
    public Query apply(String queryString) {
        Query result = querySupplier.get();
        QueryFactory.parse(result, queryString, baseURI, syntax);
        return result;
    }

    public static SparqlQueryParserImpl create(SparqlParserConfig config) {
        SparqlQueryParserImpl result = create(config.getSyntax(), config.getPrologue());
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax) {
        SparqlQueryParserImpl result = create(syntax, null);
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax, Prologue prologue) {
        Supplier<Query> querySupplier= new QuerySupplierImpl(prologue);

        SparqlQueryParserImpl result = new SparqlQueryParserImpl(querySupplier, syntax, null);
        return result;
    }

}
