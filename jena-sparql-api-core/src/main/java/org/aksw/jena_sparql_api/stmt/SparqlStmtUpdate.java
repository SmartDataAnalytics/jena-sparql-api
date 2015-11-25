package org.aksw.jena_sparql_api.stmt;

import com.hp.hpl.jena.update.UpdateRequest;

public class SparqlStmtUpdate
    extends SparqlStmtBase
{
    private UpdateRequest updateRequest;

    public SparqlStmtUpdate(UpdateRequest updateRequest) {
        super();
        this.updateRequest = updateRequest;
    }

    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    @Override
    public boolean isUpdateRequest() {
        return true;
    }

    @Override
    public SparqlStmtUpdate getAsUpdateStmt() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((updateRequest == null) ? 0 : updateRequest.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SparqlStmtUpdate other = (SparqlStmtUpdate) obj;
        if (updateRequest == null) {
            if (other.updateRequest != null)
                return false;
        } else if (!updateRequest.equals(other.updateRequest))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SparqlStatementUpdateRequest [updateRequest=" + updateRequest
                + "]";
    }
}
