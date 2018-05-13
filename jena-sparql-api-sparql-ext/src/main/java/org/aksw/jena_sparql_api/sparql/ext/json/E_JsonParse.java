package org.aksw.jena_sparql_api.sparql.ext.json;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Deprecated:
 * Instead of json:parse(?str) just parse strings as json using
 * the more standard way strdt(?str, xsd:json)
 * (maybe (xsd:json)(?str) would work too)
 * 
 * @author raven Mar 2, 2018
 *
 */
//@Deprecated
//public class E_JsonParse
//    extends FunctionBase1
//{
//    private Gson gson;
//
//    public E_JsonParse() {
//        this(new Gson());
//    }
//
//    public E_JsonParse(Gson gson) {
//        super();
//        this.gson = gson;
//    }
//
//    @Override
//    public NodeValue exec(NodeValue nv) {
//        NodeValue result;
//        if(nv.isString()) {
//            String str = nv.getString();
//            JsonElement json = gson.fromJson(str, JsonElement.class);
//
//            result = E_JsonPath.jsonToNode(json, gson);//new NodeValueJson(json);
//        } else {
//            result = NodeValue.nvNothing;
//        }
//        return result;
//    }
//
//}