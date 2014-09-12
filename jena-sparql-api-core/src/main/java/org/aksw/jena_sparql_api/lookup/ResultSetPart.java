package org.aksw.jena_sparql_api.lookup;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class ResultSetPart {
    private List<Binding> rows = new ArrayList<Binding>();
    private List<String> varNames = null ;
    
    public ResultSetPart(List<Binding> rows, List<String> varNames) {
        super();
        this.rows = rows;
        this.varNames = varNames;
    }

    public List<Binding> getRows() {
        return rows;
    }
    
    public List<String> getVarNames() {
        return varNames;
    }
}
