package org.aksw.jena_sparql_api.algebra.analysis;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

public class VarInfo {
    protected Set<Var> projectVars;
    //protected Set<Var> distinctVars;
    protected int distinctLevel; // 0: no-distinct, 1: reduced, 2: distinct

    public VarInfo(Set<Var> projectVars, int distinctLevel) {
        super();
        this.projectVars = projectVars;
        //this.distinctVars = distinctVars;
        this.distinctLevel = distinctLevel;
    }

    public Set<Var> getProjectVars() {
        return projectVars;
    }

    public int getDistinctLevel() {
        return distinctLevel;
    }

    @Override
    public String toString() {
        return "VarInfo [projectVars=" + projectVars + ", distinctLevel=" + distinctLevel + "]";
    }


    public VarInfo applyTransform(NodeTransform nodeTransform) {
        Set<Var> vars = projectVars.stream()
                .map(v -> (Var)nodeTransform.apply(v))
                .collect(Collectors.toSet());

        VarInfo result = new VarInfo(vars, distinctLevel);
        return result;
    }
}