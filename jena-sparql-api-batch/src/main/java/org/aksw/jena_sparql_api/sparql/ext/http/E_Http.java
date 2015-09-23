package org.aksw.jena_sparql_api.sparql.ext.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

/**
 * jsonLiteral jsonp(jsonLiteral, queryString)
 *
 * @author raven
 *
 */
public class E_Http
    extends FunctionBase2
{
	private HttpClient httpClient;

    public E_Http() {
        this(new DefaultHttpClient());
    }

    public E_Http(HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }
//
//    @Override
//    public NodeValue exec(NodeValue url, NodeValue query) {
//    	HttpGet request = new HttpGet(url);
//
//    	// add request header
//    	request.addHeader("User-Agent", USER_AGENT);
//    	HttpResponse response = client.execute(request);
//
//    	System.out.println("Response Code : "
//                    + response.getStatusLine().getStatusCode());
//
//    	BufferedReader rd = new BufferedReader(
//    		new InputStreamReader(response.getEntity().getContent()));
//
//        return result;
//    }

	@Override
	public NodeValue exec(NodeValue v1, NodeValue v2) {
		throw new RuntimeException("Not implemented yet");
	}
}