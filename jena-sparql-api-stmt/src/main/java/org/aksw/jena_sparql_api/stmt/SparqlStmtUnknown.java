package org.aksw.jena_sparql_api.stmt;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;

public class SparqlStmtUnknown
    extends SparqlStmtBase
{
    public SparqlStmtUnknown(String originalString, QueryParseException parseException) {
        super(originalString, parseException);
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
