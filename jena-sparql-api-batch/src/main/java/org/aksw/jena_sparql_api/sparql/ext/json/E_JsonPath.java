package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.jayway.jsonpath.JsonPath;

/**
 * jsonLiteral jsonp(jsonLiteral, queryString)
 *
 * @author raven
 *
 */
public class E_JsonPath
    extends FunctionBase2
{
    private Gson gson;

    public E_JsonPath() {
        this(new Gson());
    }

    public E_JsonPath(Gson gson) {
        super();
        this.gson = gson;
    }

    public static Object asJson(NodeValue nv, Gson gson) {
    	Node asNode = nv.asNode();
        Object result;
        if(nv instanceof NodeValueJson) {
            result = ((NodeValueJson)nv).getJson();
        } else if(asNode.getLiteralDatatype() instanceof RDFDatatypeJson) {
            result = asNode.getLiteralValue();
        } else if(nv.isString()) {
            String str = nv.getString();
            result = gson.fromJson(str, Object.class);
        } else {
            result = null;
        }

        return result;
    }

    public static NodeValue jsonToNodeValue(JsonElement e) {
    	NodeValue result;
    	if(e == null) {
    		result = NodeValue.nvNothing;
    	} else if(e.isJsonPrimitive()) {
    		JsonPrimitive p = e.getAsJsonPrimitive();
    		Object o = null;

    		if(p.isNumber()) {
    			o = p.getAsNumber();
    		} else if(p.isBoolean()) {
    			o = p.getAsBoolean();
    		}

    		if(o != null) {
	        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
	        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
	        	result = NodeValue.makeNode(node);
    		} else if(p.isString()) {
    			String str = p.getAsString();
    			result = NodeValue.makeString(str);
    		} else {
    			throw new RuntimeException("Datatype not supported " + p);
    		}
        } else if(e.isJsonObject() || e.isJsonArray()) { // arrays are json objects / array e.isJsonArray() ||
        	result = new NodeValueJson(e);
        } else {
        	throw new RuntimeException("Datatype not supported " + e);
        }

        return result;
    }

    public static NodeValue jsonToNodeValue(Object o) {
    	NodeValue result;
    	if(o == null) {
    		result = NodeValue.nvNothing;
    	} else if(o instanceof Number) {
        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
        	result = NodeValue.makeNode(node);
        } else if(o instanceof String) {
        	result = NodeValue.makeString((String)o);
        } else {
            result = new NodeValueJson(o);
        }

        return result;
    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {

    	Object json = asJson(nv, gson);

        NodeValue result;
        if(query.isString() && json != null) {
            String queryStr = query.getString();

            Object o = JsonPath.read(json, queryStr);
            result = jsonToNodeValue(o);
        } else {
            result = NodeValue.nvNothing;
        }

        return result;
    }
}