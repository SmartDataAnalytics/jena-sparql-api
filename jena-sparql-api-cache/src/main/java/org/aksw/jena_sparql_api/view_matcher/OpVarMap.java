package org.aksw.jena_sparql_api.view_matcher;

import java.util.Map;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

public class OpVarMap
{
    protected Map<Op, Op> opMapping;
    protected Iterable<Map<Var, Var>> varMapping;
    
    public OpVarMap(Map<Op, Op> opMapping,
            Iterable<Map<Var, Var>> varMapping) {
        super();
        this.varMapping = varMapping;
    }

    public Map<Op, Op> getOpMapping() {
        return opMapping;
    }

    public Iterable<Map<Var, Var>> getVarMapping() {
        return varMapping;
    }

    
}
