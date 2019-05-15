package org.aksw.jena_sparql_api.sparql.ext.url;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_UrlNormalize
	extends FunctionBase1
{
	// This still feels hacky: Instead of using URI.normalize, we use Path.normalize for the file uri scheme
	// I do not yet understand why path.normalize.toURI is different from path.toURI.normalize for paths wiouth symlinks.
	// the difference is file:///tmp vs file:/tmp
	// Update: According to https://en.wikipedia.org/wiki/File_URI_scheme 
	// file:// expects a following hostname which may be empty followed by /,
	// whereas file:/ does not. So file:/path is probably correctly normalized.
	// (however, path.normalize() only normalizes the path and does not normalize file:/// at the beginning)
    @Override
    public NodeValue exec(NodeValue nv) {
    	NodeValue result;
        Node n = nv.asNode();
        if(n.isURI()) {
        	String str = n.getURI();
        	String tmp = normalize(str);
    		result = NodeValue.makeNode(NodeFactory.createURI(tmp));
        } else {
        	throw new ExprTypeException("Not a URI: " + nv);
        }

        return result;
    }
    
    public static String normalize(String str) {
    	URI uri;
    	try {        		
    		uri = new URI(str);
    	} catch(Exception e) {
    		throw new ExprTypeException("Failed to parse URI", e);
    	}
    	String uriScheme = uri.getScheme();
    	String tmp;
    	if(uriScheme.equals("file")) {
    		tmp = Paths.get(uri).normalize().toUri().toString();
    	} else {
    		tmp = uri.normalize().toString();
    	}
    	
    	return tmp;
    }
}
