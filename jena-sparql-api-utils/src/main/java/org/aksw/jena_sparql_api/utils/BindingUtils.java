package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class BindingUtils {

//    public static Binding clone(Binding binding) {
//        Binding result = new BindingHashMap();
//    }

    public static Map<Var, Node> toMap(Binding binding) {
    	Map<Var, Node> result = new HashMap<Var, Node>();
    	Iterator<Var> it = binding.vars();
    	while(it.hasNext()) {
    		Var v = it.next();
    		Node n = binding.get(v);
    		result.put(v, n);
    	}

    	return result;
    }

    public static List<Binding> addRowIds(Collection<Binding> bindings, Var rowId) {
        List<Binding> result = new ArrayList<Binding>(bindings.size());
        long i = 0;
        for(Binding parent : bindings) {
            BindingHashMap b = new BindingHashMap(parent);
            Node node = NodeValue.makeInteger(i).asNode();
            b.add(rowId, node);
            ++i;
        }

        return result;
    }

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
