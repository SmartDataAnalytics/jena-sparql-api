package org.aksw.jena_sparql_api.concept_cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.algebra.table.TableN;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

class Utils2 {

    public static Binding transformKeys(Binding binding, NodeTransform transform) {
        Iterator<Var> it = binding.vars();

        BindingHashMap result = new BindingHashMap();
        while(it.hasNext()) {
            Var o = it.next();
            Node node = binding.get(o);

            Var n = (Var)transform.convert(o);

            result.add(n, node);
        }

        return result;
    }

    public static Table transform(Table table, NodeTransform transform) {
        List<Var> oldVars = table.getVars();

        List<Var> newVars = new ArrayList<Var>(oldVars.size());
        for(Var o : oldVars) {
            Var n = (Var)transform.convert(o);
            newVars.add(n);
        }

        //List<Binding> newBindings = new ArrayList<Binding>(table.size());
        Table result = new TableN(newVars);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding o = it.next();

            Binding n = transformKeys(o, transform);
            result.addBinding(n);;
        }

        return result;
    }

    public static Set<Var> getCooccurrentVars(Set<Var> vars, Iterable<Quad> quads) {

        Set<Var> result = new HashSet<Var>();
        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Var> intersection = Sets.intersection(vars, quadVars);
            //Set<Var> diff = Sets.difference(quadVars, vars);

            if(!intersection.isEmpty()) {
                result.addAll(quadVars);
            }

        }
        return result;
    }

    // TODO Reflexive or not? Right now its irreflexive - i.e. a var cannot co-occur with itself
    public static Multimap<Var, Var> getCooccurrentVarMap(Set<Var> vars, Iterable<Quad> quads) {

        //Set<Var> result = new HashSet<Var>();
        Multimap<Var, Var> result = HashMultimap.create();
        for(Quad quad : quads) {
            Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);

            Set<Var> intersection = Sets.intersection(vars, quadVars);
            for(Var v : intersection) {
                Set<Var> diff = Sets.difference(quadVars, Collections.singleton(v));

                result.putAll(v, diff);
            }
//            if(!intersection.isEmpty()) {
//                result.addAll(quadVars);
//            }

        }
        return result;
    }


    /**
     * Note: if we assigned variables to integers, we could use arrays rather than maps
     *
     * @param source
     * @param target
     * @return
     */
    public static Map<Var, Var> createVarMap(Quad source, Quad target) {

        Map<Var, Var> result = new HashMap<Var, Var>();

        for(int i = 0; i < 4; ++i) {
            Var s = (Var)QuadUtils.getNode(source, i);
            Var t = (Var)QuadUtils.getNode(target, i);

            Map<Var, Var> cand = Collections.singletonMap(s, t);
            boolean isCompatible = MapUtils.isPartiallyCompatible(cand, result);
            if(!isCompatible) {
                result = null;
                break;
            } else {
                result.put(s, t);
            }
        }

        return result;
    }


}