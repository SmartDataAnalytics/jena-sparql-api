package org.aksw.jena_sparql_api.json;

import java.io.PrintStream;

import org.aksw.jena_sparql_api.stmt.SPARQLResultSink;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SPARQLResultVisitorSelectJsonOutput
		implements SPARQLResultSink
	{
		protected boolean flat = false;
		protected JsonArray arr;
		protected int maxDepth = 3;
		protected Gson gson;
		protected PrintStream out;
		protected PrintStream err;
		
		public SPARQLResultVisitorSelectJsonOutput() {
			this(null, null, null, null);
		}

		public SPARQLResultVisitorSelectJsonOutput(Gson gson) {
			this(null, null, null, gson);
		}
		
		public SPARQLResultVisitorSelectJsonOutput(
				JsonArray arr,
				Integer maxDepth,
				Boolean flat,
				Gson gson) {
			this(arr, maxDepth, flat, gson, null, null);
		}

		public SPARQLResultVisitorSelectJsonOutput(
				JsonArray arr,
				Integer maxDepth,
				Boolean flat,
				Gson gson,
				PrintStream out,
				PrintStream err) {
			super();
			this.arr = arr != null ? arr : new JsonArray();
			this.maxDepth = maxDepth != null ? maxDepth : 3;
			this.flat = flat != null ? flat : false;
			this.gson = gson != null ? gson : new Gson();
			this.out = out != null ? out : System.out;
			this.err = err != null ? err : System.err;
		}

		@Override
		public void onResultSet(ResultSet value) {
			JsonElement json = RdfJsonUtils.toJson(value, maxDepth, flat);
			onJson(json);
		}

		@Override
		public void onJson(JsonElement value) {
			//String str = gson.toJson(value);
			arr.add(value);
		}

		@Override
		public void onQuad(Quad value) {
			err.println(value);
		}

		@Override
		public void close() throws Exception {
			// Return the last item of the json array
			JsonElement tmp = arr.size() == 0
					? new JsonObject()
					: arr.get(arr.size() - 1);
//			JsonElement tmp = arr.size() != 1
//					? arr
//					: arr.get(0);

            if (flat && tmp.isJsonArray() && tmp.getAsJsonArray().size() == 1) {
                tmp = tmp.getAsJsonArray().get(0);
            }
			String str = gson.toJson(tmp);
			//System.out.println(str);
			out.println(str);
		}


		@Override
		public void flush() {
			out.flush();
			err.flush();
		}

		public PrintStream getOut() {
			return out;
		}

		public PrintStream getErr() {
			return err;
		}
	}