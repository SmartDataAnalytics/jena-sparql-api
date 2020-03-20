package org.aksw.jena_sparql_api.stmt;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Quad;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * TODO We probably need a high level result visitor for
 * result types such as Model, ResultSet, Json, and a low level - or streaming -
 * result processor for quads, bindings, etc
 * The streaming part can be based on StreamRDF
 *
 * @author raven
 *
 * @param <T>
 */
public interface SPARQLResultVisitor {
//	T onModel(Model value);
//	T onDataset(Dataset value);
    void onResultSet(ResultSet value); // onBinding?
    //void onBoolean(Boolean value);
    void onJson(JsonElement value);
    void onQuad(Quad value);

    public static <T extends SPARQLResultVisitor> T forward(SPARQLResultEx sr, T handler) {
        if (sr.isQuads()) {
            //SinkQuadOutput sink = new SinkQuadOutput(System.out, null, null);
            Iterator<Quad> it = sr.getQuads();
            while (it.hasNext()) {
                Quad t = it.next();
                handler.onQuad(t);
            }
        } else if (sr.isTriples()) {
            // System.out.println(Algebra.compile(q));

            Iterator<Triple> it = sr.getTriples();
            while (it.hasNext()) {
                Triple t = it.next();
                Quad quad = new Quad(Quad.defaultGraphIRI, t);
                handler.onQuad(quad);
            }
        } else if(sr.isResultSet()) {
            ResultSet value = sr.getResultSet();
            handler.onResultSet(value);
        } else if(sr.isBoolean()) {
            Boolean value = sr.getBooleanResult();
            handler.onJson(new JsonPrimitive(value));
            //handler.onBoolean(value);
        } else if(sr.isJson()) {
            Gson gson = new Gson();
            Iterator<org.apache.jena.atlas.json.JsonObject> it = sr.getJsonItems();
            JsonArray arr = new JsonArray();
            while(it.hasNext()) {
                org.apache.jena.atlas.json.JsonObject value = it.next();
                String json = value.toString();
                JsonObject item = gson.fromJson(json, com.google.gson.JsonObject.class);
                arr.add(item);
            }
            handler.onJson(arr);
        } else if(sr.isUpdateType()) {
            // Nothing todo
        } else {
            throw new RuntimeException("Unknown SPARQL result");
        }

        return handler;
    }

//	public static <T> T forward(SPARQLResult sr, SPARQLResultVisitor<T> handler) {
//		T result;
//		if(sr.isGraph() || sr.isModel()) {
//			Model value = sr.getModel();
//			result = handler.onModel(value);
//		} else if(sr.isDataset()) {
//			Dataset value = sr.getDataset();
//			result = handler.onDataset(value);
//		} else if(sr.isResultSet()) {
//			ResultSet value = sr.getResultSet();
//			result = handler.onResultSet(value);
//		} else if(sr.isBoolean()) {
//			Boolean value = sr.getBooleanResult();
//			result = handler.onBoolean(value);
//		} else if(sr.isJson()) {
//			JsonArray value = new JsonArray();
//			sr.getJsonItems().forEachRemaining(value::add);
//			result = handler.onJson(value);
//		} else {
//			throw new RuntimeException("Unknown SPARQL result");
//		}
//		return result;
//	}
}
