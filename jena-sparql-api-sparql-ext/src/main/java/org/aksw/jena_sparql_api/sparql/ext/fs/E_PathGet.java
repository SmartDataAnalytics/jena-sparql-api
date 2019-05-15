package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_PathGet
    extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue nv) {
    	Path path;
    	if(nv.isString()) {
    		
    		path = Paths.get(nv.asString());
    	} else if(nv.asNode().isURI()) {
    		String str = nv.asNode().getURI();
    		try {
    			path = Paths.get(new URI(str));
			} catch (URISyntaxException e) {
				throw new ExprTypeException("Invalid IRI", e);
			}
    	} else {
    		throw new ExprTypeException("String or IRI expected");
    	}

		NodeValue result = NodeValue.makeNode(NodeFactory.createURI(path.toUri().toString()));
    	return result;
    }

}
