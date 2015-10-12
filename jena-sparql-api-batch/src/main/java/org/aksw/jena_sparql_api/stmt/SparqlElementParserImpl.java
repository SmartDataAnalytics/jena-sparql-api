package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.utils.ElementUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.syntax.Element;

public class SparqlElementParserImpl
    implements SparqlElementParser
{
    protected SparqlQueryParser queryParser;

    public SparqlElementParserImpl() {
        this(new SparqlQueryParserImpl());
    }

    public SparqlElementParserImpl(SparqlQueryParser queryParser) {
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
}
