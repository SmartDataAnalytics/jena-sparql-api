package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeValuePathUtils {
	public static Path toPath(NodeValue v) {
		// env.getContext().get()
		// TODO get base / iri resolver from context
		Path result;
		try {
			if(v.isIRI()) {
				String iri = v.asNode().getURI();
				result = Paths.get(new URI(iri));
				//pathStr = IRILib.IRIToFilename(iri);
			} else if(v.isString()) {
				String pathStr = v.getString();
				result = Paths.get(pathStr);
			} else {
				throw new ExprEvalException("IRI or String expected - got: " + v);
			}
			
		} catch(Exception e) {
			throw new ExprEvalException("Failed to create path from " + v);
		}
		
		return result;
	}
	
}
