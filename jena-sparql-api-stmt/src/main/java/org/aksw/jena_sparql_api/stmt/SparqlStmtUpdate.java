package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.syntax.UpdateRequestUtils;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class SparqlStmtUpdate
    extends SparqlStmtBase
{
    protected UpdateRequest updateRequest;

    public SparqlStmtUpdate(Update update) {
        this(new UpdateRequest(update));
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest) {
        this(updateRequest, updateRequest.toString());
    }

    public SparqlStmtUpdate(String updateRequestStr) {
        this(null, updateRequestStr);
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest, String updateRequestStr) {
        this(updateRequest, updateRequestStr, null);
    }

    public SparqlStmtUpdate(String updateRequestStr, QueryParseException parseException) {
        this(null, updateRequestStr, parseException);
    }

    public SparqlStmtUpdate(UpdateRequest updateRequest, String updateRequestStr, QueryParseException parseException) {
        super(updateRequestStr, parseException);
        this.updateRequest = updateRequest;
    }

    @Override
    public SparqlStmtUpdate clone() {
        UpdateRequest clone = updateRequest != null
                ? UpdateRequestUtils.clone(updateRequest)
                : null;

        return new SparqlStmtUpdate(clone, originalString, parseException);
    }


    public UpdateRequest getUpdateRequest() {
        return updateRequest;
    }

    @Override
    public boolean isParsed() {
        return updateRequest != null;
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
        int result = super.hashCode();
        result = prime * result
                + ((updateRequest == null) ? 0 : updateRequest.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
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
        String result = updateRequest != null
                ? updateRequest.toString()
                : super.toString();

        return result;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = updateRequest != null
                ? updateRequest.getPrefixMapping()
                : null;
        return result;
    }

}
