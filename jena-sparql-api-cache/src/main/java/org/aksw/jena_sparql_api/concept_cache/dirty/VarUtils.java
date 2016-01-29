package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;

class VarUtils {
    public static List<Var> toList(Collection<String> varNames) {
        List<Var> result = new ArrayList<Var>(varNames.size());
        for(String varName : varNames) {
            Var var = Var.alloc(varName);
            result.add(var);
        }

        return result;
    }

    public static Set<Var> toSet(Collection<String> varNames) {
        Set<Var> result = new HashSet<Var>();
        for(String varName : varNames) {
            Var var = Var.alloc(varName);
            result.add(var);
        }

        return result;
    }
    
}
