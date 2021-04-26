package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

    private static final Logger logger = LoggerFactory.getLogger(E_JsonPath.class);

    private Gson gson;

    public E_JsonPath() {
        this(new Gson());
    }

    public E_JsonPath(Gson gson) {
        super();
        this.gson = gson;
    }

    public static JsonElement asJson(NodeValue nv) {
        Node asNode = nv.asNode();
        JsonElement result;
        if(asNode.getLiteralDatatype() instanceof RDFDatatypeJson) {
            result = (JsonElement)asNode.getLiteralValue();
        } else {
            result = null;
        }

        return result;
    }

//    public static Node createPrimitiveNodeValue(Object o) {
//        RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
//        Node result = NodeFactory.createLiteralByValue(o, dtype);
//        return result;
//    }

    public static Node jsonToNode(Object o) {
        Gson gson = new Gson();
        RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);
        Node result = jsonToNode(o, gson, dtype);
        return result;
    }
    public static Node jsonToNode(Object o, Gson gson, RDFDatatype jsonDatatype) {
        boolean isPrimitive = o instanceof Boolean || o instanceof Number || o instanceof String;

        Node result;
        if(o == null) {
            result = null;
        } else if(isPrimitive) {
            RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
            result = NodeFactory.createLiteralByValue(o, dtype);
        } else if(o instanceof JsonElement) {
            JsonElement e = (JsonElement)o;
            result = jsonToNode(e, gson, jsonDatatype);
        } else {
            // Write the object to json and re-read it as a json-element
            String str = gson.toJson(o);
            JsonElement e = gson.fromJson(str, JsonElement.class);
            result = jsonToNode(e, gson, jsonDatatype);
        }
//    	else {
//    		throw new RuntimeException("Unknown type: " + o);
//    	}

        return result;
    }

    public static Node jsonToNode(JsonElement e, Gson gson, RDFDatatype jsonDatatype) {
        Node result;
        if(e == null) {
            result = null;
        } else if(e.isJsonPrimitive()) {
            //JsonPrimitive p = e.getAsJsonPrimitive();
            Object o = gson.fromJson(e, Object.class); //JsonTransformerUtils.toJavaObject(p);

            if(o != null) {
                RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
                result = NodeFactory.createLiteralByValue(o, dtype);
            } else {
                throw new RuntimeException("Datatype not supported " + e);
            }
        } else if(e.isJsonObject() || e.isJsonArray()) { // arrays are json objects / array e.isJsonArray() ||
            result = NodeFactory.createLiteralByValue(e, jsonDatatype);//new NodeValueJson(e);
        } else if (e.isJsonNull()) {
        	result = null;
        } else {
            throw new RuntimeException("Datatype not supported " + e);
        }

        return result;
    }

//    public static NodeValue jsonToNodeValue(Object o) {
//    	NodeValue result;
//    	if(o == null) {
//    		result = NodeValue.nvNothing;
//    	} else if(o instanceof Number) {
//        	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
//        	Node node = NodeFactory.createUncachedLiteral(o, dtype);
//        	result = NodeValue.makeNode(node);
//        } else if(o instanceof String) {
//        	result = NodeValue.makeString((String)o);
//        } else {
//            result = new NodeValueJson(o);
//        }
//
//        return result;
//    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue query) {
        RDFDatatype jsonDatatype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

        JsonElement json = asJson(nv);

        NodeValue result;
        if(query.isString() && json != null) {
            Object tmp = gson.fromJson(json, Object.class); //JsonTransformerObject.toJava.apply(json);
            String queryStr = query.getString();

            try {
                // If parsing the JSON fails, we return nothing, yet we log an error
                Object o = JsonPath.read(tmp, queryStr);

                Node node = jsonToNode(o, gson, jsonDatatype);
                result = NodeValue.makeNode(node);
            } catch(Exception e) {
                logger.warn(e.getLocalizedMessage());
                NodeValue.raise(new ExprTypeException("Error evaluating json path", e));
                result = null;
                //result = NodeValue.nvNothing;
            }

        } else {
            NodeValue.raise(new ExprTypeException("Invalid arguments to json path"));
            result = null; //NodeValue.nvNothing;
        }

        return result;
    }
}