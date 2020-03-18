package org.aksw.jena_sparql_api.sparql.ext.fs;

import java.nio.file.Path;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.tika.io.TikaInputStream;

/**
 * Probe the encoding of a file
 * Returns an empty string if none could be obtained
 * 
 * @author raven
 *
 */
public class probeEncoding
	extends FunctionBase1
{
	public static String doProbeEncoding(Path path) {
		String result;
		try(TikaInputStream in = TikaInputStream.get(path)) {
			result = CompressorStreamFactory.detect(in);
		} catch(Exception e) {
			throw new ExprEvalException("No encoding based on compression detected for " + path);
		}

		return result;
	}
	
	@Override
	public NodeValue exec(NodeValue v) {
		Path path = NodeValuePathUtils.toPath(v);
		String tmp = doProbeEncoding(path);

		NodeValue result = NodeValue.makeString(tmp);
		return result;
	}
}
