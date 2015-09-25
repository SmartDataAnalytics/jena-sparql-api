package org.aksw.jena_sparql_api.sparql.ext.http;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.sparql.ext.json.NodeValueJson;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

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

	private HttpClient httpClient;

    public E_Http() {
        this(new DefaultHttpClient());
    }

    public E_Http(HttpClient httpClient) {
        super();
        this.httpClient = httpClient;
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
			HttpGet request = new HttpGet(url);

			// add request header
			//request.addHeader("User-Agent", USER_AGENT);
			HttpResponse response = httpClient.execute(request);


			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();

			if(statusCode == 200) {
				String str = StreamUtils.toString(entity.getContent());

				Header contentType = entity.getContentType();
				String contentTypeValue = contentType.getValue();

				boolean isJson = MediaType.parse(contentTypeValue).is(MediaType.JSON_UTF_8);
				if(isJson) {
					Gson gson = new Gson();
					Object o = gson.fromJson(str, Object.class);
					result = NodeValueJson.create(o);

				} else {
					result = NodeValue.makeString(str);
				}
			}

		}

		if(result == null) {
			result = NodeValue.nvNothing;
		}

        return result;
	}
}