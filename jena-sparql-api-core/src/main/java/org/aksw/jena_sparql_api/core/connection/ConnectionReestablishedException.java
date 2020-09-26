package org.aksw.jena_sparql_api.core.connection;

import org.apache.jena.query.QueryException;

public class ConnectionReestablishedException
    extends QueryException
{
    private static final long serialVersionUID = 1L;

    public ConnectionReestablishedException(String message, Throwable cause) {
        super(message, cause);
    }
}
