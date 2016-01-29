package org.aksw.jena_sparql_api.sparql.ext.http;

import java.io.IOException;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.lib.Sink;

import com.google.common.base.Stopwatch;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

/**
 * Interceptor for HTTP requests and responses that generates RDF and puts it into
 * a sink
 *
 * @author raven
 *
 */
public class HttpInterceptorRdfLogging
    implements HttpRequestInterceptor, HttpResponseInterceptor
{
    private Sink<Model> modelSink;

    public HttpInterceptorRdfLogging(Sink<Model> modelSink) {
        super();
        this.modelSink = modelSink;
    }

    public static HttpRequest unwrap(HttpRequest result) {
        if(result instanceof RequestWrapper) {
            HttpRequest tmp = ((RequestWrapper)result).getOriginal();

            result = tmp == null ? result : unwrap(tmp);
        }

        return result;
    }

    /**
     *
     * :logMsg123
     *   a :LogMessage ;
     *   http:uri <http://localhost/foobar> ;
     *   dc:created "timestamp" ;
     *   sparql:query "query string"
     *   timeTaken: // we could use start and end time instead
     *
     *
     *
     */
    public static void writeHttpLogMsg(Model target, Resource root) {

    }

    @Override
    public void process(HttpRequest request, HttpContext context)
            throws HttpException, IOException
    {

        //String queryStr = request.getFirstHeader("query").getValue();

        Stopwatch sw = Stopwatch.createStarted();
        //System.out.println("Request started");

        HttpRequest orig = unwrap(request);

        //RequestLine rq = request.getRequestLine();
        if(orig instanceof HttpRequestBase) {
            HttpRequestBase x = (HttpRequestBase)orig;

            //System.out.println("XXXX" + x.getURI());
        }

        context.setAttribute("stopwatch", sw);
        context.setAttribute("request", orig);
    }

    @Override
    public void process(HttpResponse response, HttpContext context)
            throws HttpException, IOException {

        HttpRequestBase orig = (HttpRequestBase)context.getAttribute("request");
        URI uri = orig.getURI();
        String uriStr = uri.toString();

        //System.out.println("ARGHREQ " + uriStr);

        if(!uriStr.contains("log")) {

            Stopwatch sw = (Stopwatch)context.getAttribute("stopwatch");

            //HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            //System.out.println(statusCode);


            //response.getR
            long requestDuration = sw.elapsed(TimeUnit.MILLISECONDS);
            //System.out.println("TIME TAKEN: " + requestDuration);
            //HttpRequest r

            Model m = ModelFactory.createDefaultModel();
            String prefix = "http://example.org/log/";
            Resource s = m.createResource(prefix + "msg-" + System.nanoTime());

            String http = "http://jsa.aksw.org/ontology/http/";
            String u = "http://jsa.aksw.org/ontology/uri/";

            m.add(s, RDF.type, http + "Message");
            m.add(s, m.createProperty(u + "uri"), uri.toString());
            m.add(s, m.createProperty(u + "scheme"), uri.getScheme());
            m.add(s, m.createProperty(u + "host"), uri.getHost());
            m.add(s, m.createProperty(u + "path"), uri.getPath());
            //m.add(s, m.createProperty(u + "query"), uri.getQuery());
            m.add(s, m.createProperty(u + "port"), m.createTypedLiteral(uri.getPort()));
            m.add(s, m.createProperty(u + "fragment"), StringUtils.defaultIfEmpty(uri.getFragment(), ""));
            m.add(s, m.createProperty(u + "authority"), uri.getAuthority());


            m.add(s, DCTerms.created, m.createTypedLiteral(new GregorianCalendar()));
            m.add(s, m.createProperty(http + "duration"), m.createTypedLiteral(requestDuration));
            m.add(s, m.createProperty(http + "statusCode"), m.createTypedLiteral(statusCode));


            modelSink.send(m);
        }
        //System.out.println("Request: " + context.getAttribute("request"));
    }
}
