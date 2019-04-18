package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_Getenv
	extends FunctionBase1
{
	// This still feels hacky: Instead of using URI.normalize, we use Path.normalize for the file uri scheme
	// I do not yet understand why path.normalize.toURI is different from path.toURI.normalize.
    @Override
    public NodeValue exec(NodeValue nv) {
    	NodeValue result;
        if(nv.isString()) {
        	String key = nv.getString();
        	String value = System.getenv(key);

        	if(value == null) {
            	throw new ExprTypeException("No such system environment variable: " + nv);
        	} else {
        		result = NodeValue.makeString(value);	
        	}        	
        } else {
        	throw new ExprTypeException("Not a string argument: " + nv);
        }

        return result;
    }
}

