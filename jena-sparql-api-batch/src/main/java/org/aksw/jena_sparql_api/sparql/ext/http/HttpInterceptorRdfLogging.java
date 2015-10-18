package org.aksw.jena_sparql_api.sparql.ext.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

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
        System.out.println("Request started");

        HttpRequest orig = unwrap(request);

        //RequestLine rq = request.getRequestLine();
        if(orig instanceof HttpRequestBase) {
            HttpRequestBase x = (HttpRequestBase)orig;

            System.out.println("XXXX" + x.getURI());
        }

        context.setAttribute("stopwatch", sw);
        context.setAttribute("request", orig);
    }

    @Override
    public void process(HttpResponse response, HttpContext context)
            throws HttpException, IOException {
        Stopwatch sw = (Stopwatch)context.getAttribute("stopwatch");

        HttpEntity entity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();
System.out.println(statusCode);


        HttpRequestBase orig = (HttpRequestBase)context.getAttribute("request");
        String uri = orig.getURI().toString();
        if(!uri.contains("log")) {


            //response.getR
            System.out.println("TIME TAKEN: " + sw.elapsed(TimeUnit.MILLISECONDS));
            //HttpRequest r

            Model model = ModelFactory.createDefaultModel();
            String prefix = "http://example.org/log/";
            Resource s = model.createResource(prefix + "msg-" + System.nanoTime());

            model.add(s, RDFS.label, s);

            modelSink.send(model);
        }
        System.out.println("Request: " + context.getAttribute("request"));
    }
}
