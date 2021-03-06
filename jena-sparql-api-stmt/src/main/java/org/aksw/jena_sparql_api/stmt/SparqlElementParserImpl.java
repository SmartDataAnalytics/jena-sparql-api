package org.aksw.jena_sparql_api.stmt;

import java.util.function.Function;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.syntax.Element;

public class SparqlElementParserImpl
    implements SparqlElementParser
{
    protected Function<String, Query> queryParser;

    public SparqlElementParserImpl() {
        this(new SparqlQueryParserImpl());
    }

    public SparqlElementParserImpl(Function<String, Query> queryParser) {
        this.queryParser = queryParser;
    }

    @Override
    public Element apply(String elementStr) {
        String tmp = elementStr.trim();
        boolean isEnclosed = tmp.startsWith("{") && tmp.endsWith("}");
        if(!isEnclosed) {
            tmp = "{" + tmp + "}";
        }

        //ParserSparql10 p;
        tmp = "Select * " + tmp;

        Query query = queryParser.apply(tmp);
        Element raw = query.getQueryPattern();
        Element result = ElementUtils.flatten(raw);

        return result;
    }

    public static SparqlElementParserImpl create(Syntax syntax, Prologue prologue) {
        SparqlQueryParser queryParser = SparqlQueryParserImpl.create(syntax, prologue);
        SparqlElementParserImpl result = new SparqlElementParserImpl(queryParser);
        return result;
    }
}
