package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransform;

public class BindingUtils {

//    public static Binding clone(Binding binding) {
//        Binding result = new BindingHashMap();
//    }

    public static Binding project(Binding binding, Iterable<Var> vars) {
        BindingBuilder builder = BindingBuilder.create();

        for(Var var : vars) {
            Node node = binding.get(var);
            if (node != null) {
                builder.add(var, node);
            }
        }

        return builder.build();
    }

    public static Binding fromMap(Map<? extends Var, ? extends Node> map) {
        BindingBuilder builder = BindingBuilder.create();
        for(Entry<? extends Var, ? extends Node> e : map.entrySet()) {
            builder.add(e.getKey(), e.getValue());
        }
        return builder.build();
    }

    public static Binding transformKeys(Binding binding, NodeTransform transform) {
        Iterator<Var> it = binding.vars();

        BindingBuilder builder = BindingBuilder.create();
        while(it.hasNext()) {
            Var o = it.next();
            Node node = binding.get(o);

            Var n = (Var)transform.apply(o);

            builder.add(n, node);
        }

        return builder.build();
    }

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
        BindingBuilder builder = BindingBuilder.create();
        for(Binding parent : bindings) {
            builder.reset();
            builder.addAll(parent);
            Node node = NodeValue.makeInteger(i).asNode();
            builder.add(rowId, node);
            ++i;
            result.add(builder.build());
        }

        return result;
    }

    public static Binding rename(Binding binding, Map<Var, Var> varMap) {
        BindingBuilder builder = BindingBuilder.create();

        Iterator<Var> itVars = binding.vars();
        while(itVars.hasNext()) {
            Var sourceVar = itVars.next();

            Node node = binding.get(sourceVar);

            Var targetVar = varMap.get(sourceVar);
            if(targetVar == null) {
                targetVar = sourceVar;
            }

            builder.add(targetVar, node);
        }

        return builder.build();
    }

}
