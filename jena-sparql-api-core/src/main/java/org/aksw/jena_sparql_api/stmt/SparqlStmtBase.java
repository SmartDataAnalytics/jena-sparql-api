package org.aksw.jena_sparql_api.stmt;

public abstract class SparqlStmtBase
    implements SparqlStmt
{
    protected String originalString;
    protected Exception parseException;

    public SparqlStmtBase(String originalString) {
        this(originalString, null);
    }

    public SparqlStmtBase(String originalString, Exception parseException) {
        super();
        this.originalString = originalString;
        this.parseException = parseException;
    }

    @Override
    public String getOriginalString() {
        return originalString;
    }


    @Override
    public Exception getParseException() {
        return parseException;
    }


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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((originalString == null) ? 0 : originalString.hashCode());
        result = prime * result
                + ((parseException == null) ? 0 : parseException.hashCode());
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
        SparqlStmtBase other = (SparqlStmtBase) obj;
        if (originalString == null) {
            if (other.originalString != null)
                return false;
        } else if (!originalString.equals(other.originalString))
            return false;
        if (parseException == null) {
            if (other.parseException != null)
                return false;
        } else if (!parseException.equals(other.parseException))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SparqlStmtBase [originalString=" + originalString
                + ", parseException=" + parseException + "]";
    }


}
