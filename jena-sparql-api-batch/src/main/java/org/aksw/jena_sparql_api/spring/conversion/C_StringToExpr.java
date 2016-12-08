package org.aksw.jena_sparql_api.spring.conversion;

import org.aksw.jena_sparql_api.stmt.SparqlExprParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import org.apache.jena.sparql.expr.Expr;

@AutoRegistered
public class C_StringToExpr
    implements Converter<String, Expr>
{
    private SparqlExprParser parser;

//    public C_StringToQuery() {
//        System.out.println("Created class " + this.getClass().getName());
//    }

    public SparqlExprParser getParser() {
        return parser;
    }

    @Autowired
    public void setParser(SparqlExprParser parser) {
        this.parser = parser;
    }

    public Expr convert(String str) {
        Expr result = parser.apply(str);
        return result;
    }
}