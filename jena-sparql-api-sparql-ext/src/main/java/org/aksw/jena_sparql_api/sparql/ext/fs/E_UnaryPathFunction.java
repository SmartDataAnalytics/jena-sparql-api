package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.nio.file.Path;

import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionFactory;

public class E_UnaryPathFunction
	extends FunctionBase1
{
	protected PathFunction fn;
	
	public static FunctionFactory newFactory(PathFunction pathFunction) {
		return new FunctionFactory() {			
			@Override
			public Function create(String uri) {
				return new E_UnaryPathFunction(pathFunction);
			}
		};
	}
	
	public E_UnaryPathFunction(PathFunction fn) {
		super();
		this.fn = fn;
	}
	
	@Override
	public NodeValue exec(NodeValue nv) {
		Path path = NodeValuePathUtils.toPath(nv);
		NodeValue result;
		try {
			result = fn.apply(path);
		} catch (Exception e) {
			throw new ExprTypeException("Error: ", e);
		}

		return result;
		
//		NodeValue result;
//		if(nv.asNode().isURI()) {
//			String str = nv.asNode().getURI();
//			try {
//				Path path = Paths.get(new URI(str));
//				try {
//					result = fn.apply(path);
//				} catch (IOException e) {
//					throw new ExprTypeException("IO Error", e);
//				}
//			} catch (URISyntaxException e) {
//				throw new ExprTypeException("Invalid IRI", e);
//			}
//		} else {
//			throw new ExprTypeException("File URL expected");
//		}	
//		return result;
	}
}
