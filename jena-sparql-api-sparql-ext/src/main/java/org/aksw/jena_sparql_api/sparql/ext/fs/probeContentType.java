package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class probeContentType
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue v) {
		String tmp;
		try {
			Path path = NodeValuePathUtils.toPath(v);
			tmp = Files.probeContentType(path);
		} catch(Exception e) {
			throw new ExprEvalException(e);
		}

		NodeValue result = NodeValue.makeString(tmp);
		return result;
	}
}
