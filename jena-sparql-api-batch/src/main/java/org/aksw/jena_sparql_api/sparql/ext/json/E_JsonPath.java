package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.Gson;
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

    @Override
    public NodeValue exec(NodeValue obj, NodeValue query) {


        Object json;
        if(obj instanceof NodeValueJson) {
            json = ((NodeValueJson)obj).getJson();
        } else if(obj.asNode().getLiteralDatatype() instanceof RDFDatatypeJson) {
            json = obj.asNode().getLiteralValue();
        } else if(obj.isString()) {
            String str = obj.getString();
            json = gson.fromJson(str, Object.class);
        } else {
            json = null;
        }

        NodeValue result;
        if(query.isString() && json != null) {
            String queryStr = query.getString();

            Object o = JsonPath.read(json, queryStr);
            if(o instanceof Number) {
            	RDFDatatype dtype = TypeMapper.getInstance().getTypeByValue(o);
            	Node node = NodeFactory.createUncachedLiteral(o, dtype);
            	result = NodeValue.makeNode(node);
            } else if(o instanceof String) {
            	result = NodeValue.makeString((String)o);
            } else {
                result = new NodeValueJson(o);
            }
            
        } else {
            result = NodeValue.nvNothing;
        }

        return result;
    }
}