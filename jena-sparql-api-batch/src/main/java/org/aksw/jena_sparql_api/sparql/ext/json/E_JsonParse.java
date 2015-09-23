package org.aksw.jena_sparql_api.sparql.ext.json;

import com.google.gson.Gson;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class E_JsonParse
    extends FunctionBase1
{
    private Gson gson;

    public E_JsonParse() {
        this(new Gson());
    }

    public E_JsonParse(Gson gson) {
        super();
        this.gson = gson;
    }

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        if(nv.isString()) {
            String str = nv.getString();
            Object json = gson.fromJson(str, Object.class);

            result = new NodeValueJson(json);
        } else {
            result = NodeValue.nvNothing;
        }
        return result;
    }

}