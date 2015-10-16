package org.aksw.jena_sparql_api.sparql.ext.http;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import com.google.common.base.Stopwatch;

public class HttpRequestInterceptorLogging
    implements HttpRequestInterceptor, HttpResponseInterceptor
{
    @Override
    public void process(HttpRequest request, HttpContext context)
            throws HttpException, IOException
    {
        String queryStr = request.getFirstHeader("query").getValue();

        Stopwatch sw = Stopwatch.createStarted();

        context.setAttribute("stopwatch", sw);
    }

    @Override
    public void process(HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        Stopwatch sw = (Stopwatch)context.getAttribute("stopwatch");
        System.out.println("TIME TAKEN: " + sw.elapsed(TimeUnit.MILLISECONDS));
    }
}
