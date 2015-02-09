package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;

public class VarUtils {

    /**
     * Returns a list of variable names as strings for a given iterable of Var objects.
     *
     * @param vars
     * @return
     */
    public static List<String> getVarNames(Iterable<Var> vars) {
        List<String> result = new ArrayList<String>();

        for(Var var : vars) {
            result.add(var.getName());
        }

        return result;
    }

    public static List<String> map(Collection<String> varNames, Map<Var, Var> varMap) {
        List<String> result = new ArrayList<String>(varNames.size());
        for(String varName : varNames) {
            Var sourceVar = Var.alloc(varName);
            Var targetVar = varMap.get(sourceVar);

            if(targetVar == null) {
                targetVar = sourceVar;
            }

            String targetVarName = targetVar.getVarName();
            result.add(targetVarName);
        }

        return result;
    }

}