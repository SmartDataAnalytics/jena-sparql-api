package org.aksw.jena_sparql_api.sparql.ext.http;

import java.nio.charset.Charset;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.sparql.ext.json.NodeValueJson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.springframework.util.StreamUtils;

import com.google.common.net.MediaType;

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
            url = null;
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
                    String str = StreamUtils.copyToString(entity.getContent(), Charset.forName("UTF-8"));

                    Header contentType = entity.getContentType();
                    String contentTypeValue = contentType.getValue();

                    boolean isJson = MediaType.parse(contentTypeValue).is(MediaType.JSON_UTF_8);
                    if(isJson) {
                        result = NodeValueJson.create(str);
                    } else {
                        result = NodeValue.makeString(str);
                    }
                }
                EntityUtils.consume(entity);
            } catch(Exception e) {
                if(request != null) {
                    request.releaseConnection();
                }
                throw new RuntimeException(e);
            }
            //EntityUtils.consume(entity);
            //entity.

        }

        if(result == null) {
            result = NodeValue.nvNothing;
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