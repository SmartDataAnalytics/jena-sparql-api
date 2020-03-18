package org.aksw.jena_sparql_api.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RdfJsonUtils {
	public static JsonElement toJson(RDFNode rdfNode, int maxDepth, boolean flat) {
		JsonElement result = toJson(rdfNode, 0, maxDepth, flat);
		return result;
	}

	public static JsonObject toJson(Resource rdfNode, int maxDepth, boolean flat) {
		JsonElement tmp = toJson(rdfNode, 0, maxDepth, flat);
		JsonObject result = tmp.getAsJsonObject();
		return result;
	}
	
	public static JsonArray toJson(Collection<? extends RDFNode> rdfNodes, int maxDepth, boolean flat) {
		JsonArray result = new JsonArray();
		for(RDFNode rdfNode : rdfNodes) {
			JsonElement jsonElement = toJson(rdfNode, maxDepth, flat);
			result.add(jsonElement);
		}
		return result;
	
	}
	
	public static JsonArray toJson(ResultSet rs, int maxDepth, boolean flat) {
		JsonArray result = new JsonArray();
		List<String> vars = rs.getResultVars();
		while(rs.hasNext()) {
			JsonObject row = new JsonObject();
			QuerySolution qs = rs.next();
			for (String var : vars) {
				RDFNode rdfNode = qs.get(var);
				JsonElement jsonElement = toJson(rdfNode, maxDepth, flat);
				row.add(var, jsonElement);
			}
			if (flat && vars.size() == 1) {
				result.add(row.entrySet().iterator().next().getValue());
			} else {
				result.add(row);
			}
		}
		
		return result;
	}
	
	public static JsonElement toJson(RDFNode rdfNode, int depth, int maxDepth, boolean flat) {
		JsonElement result;

		if(depth >= maxDepth) {
			// TODO We could add properties indicating that data was cut off here
			result = null; // new JsonObject();
		} else if(rdfNode == null) {
			result = JsonNull.INSTANCE;
		} else if(rdfNode.isLiteral()) {
			Node node = rdfNode.asNode();
			Object obj = node.getLiteralValue();
			//boolean isNumber =//NodeMapperRdfDatatype.canMapCore(node, Number.class);
			//if(isNumber) {
			if(obj instanceof String) {
				String value = (String)obj;
				result = new JsonPrimitive(value);
			} else if(obj instanceof Number) {
				Number value = (Number)obj; //NodeMapperRdfDatatype.toJavaCore(node, Number.class);
//				Literal literal = rdfNode.asLiteral();
				result = new JsonPrimitive(value);	
			} else if(obj instanceof Boolean) {
				Boolean value = (Boolean) obj;
				result = new JsonPrimitive(value);
			} else if(obj instanceof GeometryWrapper) {
				WKTReader wktReader = new WKTReader();
				try {
					final Geometry geom = wktReader.read(rdfNode.asLiteral().getLexicalForm());

					GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
					String jsonString = geoJsonWriter.write(geom);
					Gson gson = new Gson();
					result = gson.fromJson(jsonString, JsonObject.class);
				} catch (ParseException e) {
					throw new RuntimeException("Invalid WKT : " + rdfNode);
				}
			} else {

				String value = rdfNode.asLiteral().getLexicalForm() ; // Objects.toString(obj);
				result = new JsonPrimitive(value ) ; //+ "^^" + obj.getClass().getCanonicalName());
//				throw new RuntimeException("Unsupported literal: " + rdfNode);
			}
		} else if(rdfNode.isResource()) {
			JsonObject tmp = new JsonObject();
			Resource r = rdfNode.asResource();
			
			if(r.isURIResource()) {
				tmp.addProperty("id", r.getURI());
				tmp.addProperty("id_type", "uri");
			} else if(r.isAnon()) {
				tmp.addProperty("id", r.getId().getLabelString());				
				tmp.addProperty("id_type", "bnode");
			}
			
			List<Statement> stmts = r.listProperties().toList();


			
			Map<Property, List<RDFNode>> pos = stmts.stream()
					.collect(Collectors.groupingBy(Statement::getPredicate,
							Collectors.mapping(Statement::getObject, Collectors.toList())));

			for(Entry<Property, List<RDFNode>> e : pos.entrySet()) {
				JsonArray arr = new JsonArray();
				Property p = e.getKey();
				String k = p.getLocalName();

				for(RDFNode o : e.getValue()) {
					JsonElement v = toJson(o, depth + 1, maxDepth, flat);
					if (v != null)
					arr.add(v);
				}

				if (arr.size() > 0) {
					if (flat && arr.size() == 1)
						tmp.add(k, arr.get(0));
					else
						tmp.add(k, arr);
				}
			}
			result = tmp;
		} else {
			throw new RuntimeException("Unknown node type: " + rdfNode);
		}
		
		return result;
	}
}
