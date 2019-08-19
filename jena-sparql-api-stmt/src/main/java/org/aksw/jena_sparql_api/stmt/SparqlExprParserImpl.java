package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

public class SparqlExprParserImpl
    implements SparqlExprParser
{
    protected PrefixMapping prefixMapping;

    public SparqlExprParserImpl() {
        this(null);
    }

    public SparqlExprParserImpl(PrefixMapping prefixMapping) {
        super();
        this.prefixMapping = prefixMapping;
    }

    @Override
    public Expr apply(String exprStr) {
        Expr result = ExprUtils.parse(exprStr, prefixMapping);
        return result;
    }
}
