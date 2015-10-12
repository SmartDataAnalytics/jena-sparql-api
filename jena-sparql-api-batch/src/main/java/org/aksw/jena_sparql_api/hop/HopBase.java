package org.aksw.jena_sparql_api.hop;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

public class HopBase {
    protected QueryExecutionFactory qef;

    public HopBase(QueryExecutionFactory qef) {
        super();
        this.qef = qef;
    }

    public QueryExecutionFactory getQef() {
        return qef;
    }

    @Override
    public String toString() {
        return "HopBase [qef=" + qef + "]";
    }
}
