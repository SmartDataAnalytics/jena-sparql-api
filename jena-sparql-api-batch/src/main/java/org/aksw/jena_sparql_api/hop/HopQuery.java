package org.aksw.jena_sparql_api.hop;

import org.aksw.jena_sparql_api.mapper.MappedQuery;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.DatasetGraph;

public class HopQuery
    extends HopBase
{
    protected MappedQuery<DatasetGraph> mappedQuery;

    public HopQuery(MappedQuery<DatasetGraph> mappedQuery, SparqlQueryConnection qef) {
        super(qef);
        this.mappedQuery = mappedQuery;
    }

    public MappedQuery<DatasetGraph> getMappedQuery() {
        return mappedQuery;
    }

    @Override
    public String toString() {
        return "HopQuery [mappedQuery=" + mappedQuery + ", qef="
                + super.toString() + "]";
    }
}
