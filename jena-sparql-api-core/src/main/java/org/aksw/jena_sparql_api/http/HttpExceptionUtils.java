package org.aksw.jena_sparql_api.http;

import org.apache.jena.atlas.web.HttpException;

import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;

public class HttpExceptionUtils {
    public static RuntimeException makeHumanFriendly(Exception e) {
        RuntimeException result;
        if(e instanceof HttpException) {
            HttpException x = (HttpException)e;
            result = new HttpException(x.getResponse(), e);
        } else if(e instanceof  QueryExceptionHTTP) {
            QueryExceptionHTTP x = (QueryExceptionHTTP)e;
            result = new QueryExceptionHTTP(x.getResponseMessage() + " ", e);
        } else {
            result = new RuntimeException(e);
        }

        return result;
    }
}
