package org.aksw.jena_sparql_api.stmt;

import java.io.PrintStream;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;

/**
 * Print non-quad results by default to stderr
 * 
 * @author raven
 *
 */
public class SPARQLResultSinkPrint
	implements SPARQLResultSink
{
	protected Gson gson;
	protected PrintStream out;

	public SPARQLResultSinkPrint() {
		this(null, null);
	}

	public SPARQLResultSinkPrint(PrintStream out) {
		this(out, null);
	}

	public SPARQLResultSinkPrint(PrintStream out, Gson gson) {
		super();
		this.out = out != null ? out : System.err;
		this.gson = gson != null ? gson : gson;
	}
	
	@Override
	public void onResultSet(ResultSet rs) {
		String str = ResultSetFormatter.asText(rs);
		out.println(str);
	}
	
	@Override
	public void onJson(com.google.gson.JsonElement value) {
		String json = gson.toJson(value);
		out.println(json);
	}


	@Override
	public void onQuad(Quad value) {
	}

	@Override
	public void close() throws Exception {
		
	}

	@Override
	public void flush() {
		out.flush();
	}

}
