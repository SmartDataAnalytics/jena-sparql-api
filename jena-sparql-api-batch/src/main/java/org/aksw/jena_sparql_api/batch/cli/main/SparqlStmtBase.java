package org.aksw.jena_sparql_api.batch.cli.main;

public class SparqlStmtBase
    implements SparqlStmt
{
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public boolean isUpdateRequest() {
        return false;
    }

    @Override
    public SparqlStmtUpdate getAsUpdateStmt() {
        throw new RuntimeException("Invalid type");    }

    @Override
    public SparqlStmtQuery getAsQueryStmt() {
        throw new RuntimeException("Invalid type");
    }
}
