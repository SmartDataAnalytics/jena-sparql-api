package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;

public class SparqlQueryParserImpl
    implements SparqlQueryParser
{
    protected Supplier<Query> querySupplier;
    protected Syntax syntax;
    protected String baseURI;

    public SparqlQueryParserImpl() {
        this(new QuerySupplierImpl(), Syntax.syntaxARQ, "http://example.org/base/");
    }

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

    public static SparqlQueryParserImpl create() {
        SparqlQueryParserImpl result = create(Syntax.syntaxARQ, null);
        return result;
    }

    public static SparqlQueryParserImpl create(PrefixMapping prefixMapping) {
        SparqlQueryParserImpl result = create(Syntax.syntaxARQ, new Prologue(prefixMapping));
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax) {
        SparqlQueryParserImpl result = create(syntax, null);
        return result;
    }

    public static SparqlQueryParserImpl create(Syntax syntax, Prologue prologue) {
        Supplier<Query> querySupplier = new QuerySupplierImpl(prologue);

        SparqlQueryParserImpl result = new SparqlQueryParserImpl(querySupplier, syntax, null);
        return result;
    }

    // TODO Move to common query parser utils
    public static SparqlQueryParser wrapWithOptimizePrefixes(Function<String, Query> delegate) {
        return str -> {
            Query r = delegate.apply(str);
            QueryUtils.optimizePrefixes(r);
            return r;
        };
    }
}
