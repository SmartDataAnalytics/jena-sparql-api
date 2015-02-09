package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;

public class BindingUtils {

    public static Binding rename(Binding binding, Map<Var, Var> varMap) {
        BindingHashMap result = new BindingHashMap();

        Iterator<Var> itVars = binding.vars();
        while(itVars.hasNext()) {
            Var sourceVar = itVars.next();

            Node node = binding.get(sourceVar);

            Var targetVar = varMap.get(sourceVar);
            if(targetVar == null) {
                targetVar = sourceVar;
            }

            result.add(targetVar, node);
        }

        return result;
    }

}
