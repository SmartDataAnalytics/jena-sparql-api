package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;

public class SparqlStmtUnknown
    extends SparqlStmtBase
{
    private static final long serialVersionUID = 1L;

    public SparqlStmtUnknown() {
        super(null);
    }

    public SparqlStmtUnknown(String originalString, QueryParseException parseException) {
        super(originalString, parseException);
    }

    private Object readResolve() {
        SparqlStmt tmp = SparqlStmtParserImpl.createAsGiven(true).apply(originalString);
        return new SparqlStmtUnknown(originalString, tmp.getParseException());
    }


    @Override
    public boolean isUnknown() {
        return true;
    }

    @Override
    public boolean isParsed() {
        return false;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return null;
    }

    @Override
    public SparqlStmt clone() {
        return new SparqlStmtUnknown(originalString, parseException);
    }

}
