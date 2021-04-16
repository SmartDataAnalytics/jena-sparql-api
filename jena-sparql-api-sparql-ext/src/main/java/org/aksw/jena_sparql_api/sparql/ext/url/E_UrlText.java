package org.aksw.jena_sparql_api.sparql.ext.url;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.common.io.CharStreams;

public class E_UrlText
    extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        try {
            result = resolve(nv);
        } catch (Exception e) {
        	throw new ExprEvalException("Failed to resolve URL from " + nv);//": " + node)) ;
            //throw new RuntimeException(e);
        }

        return result;
    }

    public static NodeValue resolve(NodeValue nv) throws Exception {

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
        	URI uri = new URI(url);
        	URL u = uri.toURL();
        	URLConnection conn = u.openConnection();
        	String contentType = conn.getContentType();

        	// TODO Add support for content types, e.g. parsing json
        	
        	InputStream in = conn.getInputStream();
        	
        	String str = CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        	
            result = NodeValue.makeString(str);
        }

        if(result == null) {
            // result = NodeValue.nvNothing;
        	throw new ExprEvalException("Failed to obtain text from node " + nv);
        }

        return result;
    }
}
