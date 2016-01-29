package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.engine.binding.Binding;

public class ResultSetPart {
    private List<String> varNames = null ;
    private List<Binding> rows = new ArrayList<Binding>();

    public ResultSetPart(List<String> varNames) {
        super();
        this.varNames = varNames;
    }

    public ResultSetPart(List<String> varNames, List<Binding> rows) {
        super();
        this.varNames = varNames;
        this.rows = rows;
    }

    public List<Binding> getBindings() {
        return rows;
    }

    public List<String> getVarNames() {
        return varNames;
    }
}
