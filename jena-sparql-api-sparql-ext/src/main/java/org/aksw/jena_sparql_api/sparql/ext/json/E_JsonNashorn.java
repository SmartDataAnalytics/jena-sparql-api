package org.aksw.jena_sparql_api.sparql.ext.json;

import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jdk.nashorn.api.scripting.JSObject;

public class E_JsonNashorn extends FunctionBase {
    protected ScriptEngine engine;
    protected Gson gson;

    protected JSObject jsonParse;
    protected JSObject jsonStringify;

    public E_JsonNashorn() throws ScriptException {
        this(new ScriptEngineManager().getEngineByName("nashorn"), new Gson());
    }


    public E_JsonNashorn(ScriptEngine engine, Gson gson) throws ScriptException {
        this.engine = engine;
        this.gson = gson;


        jsonParse = (JSObject) engine.eval("function(x) { return JSON.parse(x); }");
        jsonStringify = (JSObject) engine.eval("function(x) { return JSON.stringify(x); }");
    }

    public static E_JsonNashorn create() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        Gson gson = new Gson();
        return new E_JsonNashorn(engine, gson);
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        NodeValue result = null;
        NodeValue fnNv = args.get(0);
        JSObject fn = null;
        if(fnNv.isString()) {
            String str = fnNv.asUnquotedString();
            try {
                fn = (JSObject)engine.eval(str);
            } catch (ScriptException e) {
                throw new ExprEvalException(e);
            }

            List<Object> jsos = args.subList(1, args.size()).stream()
                .map(E_JsonPath::asJson)
                .map(Object::toString)
                .map(item -> jsonParse.call(null, item))
                .collect(Collectors.toList());

            RDFDatatype dtype = TypeMapper.getInstance().getTypeByClass(JsonElement.class);

            JSObject[] as = jsos.toArray(new JSObject[0]);
            Object raw = fn.call(fn, as);
            String jsonStr = jsonStringify.call(null, raw).toString();
            JsonElement jsonEl = gson.fromJson(jsonStr, JsonElement.class);
            Node node = E_JsonPath.jsonToNode(jsonEl, gson, dtype);
            result = NodeValue.makeNode(node);
        }
        return result;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        if(args.size() < 1) {
            throw new RuntimeException("At least 1 argument required for JavaScript call");
        }
    }

//    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
//
//        Gson gson = new Gson();
//
//        Object x = jsonParse.call(null, "3");
//
//        // define an anoymous function
//        JSObject multiply = (JSObject) e.eval("function(x, y) { return x*y; }");
//
//        String resultStr = jsonStringify.call(null, x).toString();
//        System.out.println("STR: " + resultStr);
//
//        // call that anon function
//        System.out.println(multiply.call(null, x, 5));
//
//        // define another anon function
//        JSObject greet = (JSObject) e.eval("function(n) { print('Hello ' + n)}");
//        greet.call(null, "nashorn");
//    }

}
