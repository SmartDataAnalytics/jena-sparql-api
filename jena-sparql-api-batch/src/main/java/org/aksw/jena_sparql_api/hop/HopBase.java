package org.aksw.jena_sparql_api.hop;

import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class HopBase {
    protected SparqlQueryConnection qef;

    public HopBase(SparqlQueryConnection qef) {
        super();
        this.qef = qef;
    }

    public SparqlQueryConnection getQef() {
        return qef;
    }

    @Override
    public String toString() {
        return "HopBase [qef=" + qef + "]";
    }
}
