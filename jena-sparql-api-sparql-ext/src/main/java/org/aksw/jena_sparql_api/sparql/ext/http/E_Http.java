package org.aksw.jena_sparql_api.sparql.ext.http;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;
import com.google.gson.JsonElement;

/**
 * jsonLiteral jsonp(jsonLiteral, queryString)
 *
 * http://www.mkyong.com/java/apache-httpclient-examples/
 *
 * http:request(concat(?baseUrl, )
 *
 * json:parse('')
 *
 * @author raven
 *
 */
public class E_Http
    extends FunctionBase1
{
	private static final Logger logger = LoggerFactory.getLogger(E_Http.class);
	
    //public static final MimeType mtJson = new MimeType("application/json");

    private Supplier<HttpClient> httpClientSupplier;

    public E_Http() {
        this(() -> new DefaultHttpClient());
    }

    public E_Http(HttpClient httpClient) {
        this(() -> httpClient);
    }

    public E_Http(Supplier<HttpClient> httpClientSupplier) {
        super();
        this.httpClientSupplier = httpClientSupplier;
    }

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        try {
            result = _exec(nv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public NodeValue _exec(NodeValue nv) throws Exception {
//        if(true) {
//            return NodeValue.nvNothing;
//        }

        String url;
        if(nv.isString()) {
            url = nv.getString();
        } else if(nv.isIRI()) {
            Node node = nv.asNode();
            url = node.getURI();
        } else {
        	throw new ExprEvalException("Neither IRI nor string");
        }

        NodeValue result = null;
        if(url != null) {
            HttpGet request = null;
            try {
                request = new HttpGet(url);

                //System.out.println("HTTP Request: " + request);

                // add request header
                //request.addHeader("User-Agent", USER_AGENT)
                HttpClient httpClient = httpClientSupplier.get();
                HttpResponse response = httpClient.execute(request);


                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if(statusCode == 200) {
                    //String str = StreamUtils.toString(entity.getContent());

                    String str = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8); //, Charset.forName("UTF-8"));

                    Header contentType = entity.getContentType();
                    String contentTypeValue = contentType.getValue();

                    boolean isJson = MediaType.parse(contentTypeValue).is(MediaType.JSON_UTF_8);
                    if(isJson) {
                    	RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);
                        Node jsonNode = NodeFactory.createLiteral(str, jsonDatatype);
                    	result = NodeValue.makeNode(jsonNode);
                    } else {
                        result = NodeValue.makeString(str);
                    }
                }
                EntityUtils.consume(entity);
            } catch(Exception e) {         
            	logger.warn("Http request failed", e);
                throw new ExprEvalException(e);
            } finally {
                if(request != null) {
                    request.releaseConnection();
                }
            }
            //EntityUtils.consume(entity);
            //entity.

        }
        
        if (result == null) {
        	// TODO Redirects should be followed automatically - is this actually the case?
        	throw new ExprEvalException("Http request returned non 200 status code");
        }

        return result;
    }

//    public static void main(String[] args) {
//        E_Http expr = new E_Http();
//        for(int i  = 0; i < 1000; ++i) {
//            NodeValue res = expr.exec(NodeValue.makeNode(NodeFactory.createURI("http://cstadler.aksw.org")));
//            System.out.println(res);
//        }
//    }
}