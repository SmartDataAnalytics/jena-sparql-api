package org.aksw.jena_sparql_api.sparql.ext.http;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * Just an experimental wrapper that delegates all method calls
 * to an unterlying HttpClient.
 *
 * I think HttpRequestInterceptor is what I am looking for though.
 *
 * @author raven
 *
 */
public class HttpClientDelegate implements HttpClient {
    protected HttpClient delegate;

    @Override
    public HttpParams getParams() {
        HttpParams result = delegate.getParams();
        return result;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        ClientConnectionManager result = delegate.getConnectionManager();
        return result;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request)
            throws IOException, ClientProtocolException {
        HttpResponse result = delegate.execute(request);
        return result;
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        HttpResponse result = delegate.execute(request, context);
        return result;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request)
            throws IOException, ClientProtocolException {
        HttpResponse result = delegate.execute(target, request);
        return result;
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request,
            HttpContext context) throws IOException, ClientProtocolException {
        HttpResponse result = delegate.execute(target, request, context);
        return result;
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler)
                    throws IOException, ClientProtocolException {
        T result = delegate.execute(request, responseHandler);
        return result;
    }

    @Override
    public <T> T execute(HttpUriRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
                    throws IOException, ClientProtocolException {
        T result = delegate.execute(request, responseHandler, context);
        return result;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler)
                    throws IOException, ClientProtocolException {
        T result = delegate.execute(target, request, responseHandler);
        return result;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request,
            ResponseHandler<? extends T> responseHandler, HttpContext context)
                    throws IOException, ClientProtocolException {
        T result = delegate.execute(target, request, responseHandler, context);
        return result;
    }
}
