package org.aksw.jena_sparql_api.concept_cache.combinatorics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Utils2 {

//    public static int getCombinationCount(long n, long k) {
//        if(k > n) {
//            result = 0;
//        } else {
//            result = (int)(CombinatoricsUtils.factorial(n) / CombinatoricsUtils.factorial(n - k));
//        }
//    }

    public static int getNumMatches(Entry<? extends Collection<?>, ? extends Collection<?>> quadGroup) {
        int result;

        int c = quadGroup.getKey().size();
        int q = quadGroup.getValue().size();

        if(c > q) { // If there are more candidates quads than query quads, we can't map all of them
            result = 0;
        } else {
            result = (int)(CombinatoricsUtils.factorial(q) / CombinatoricsUtils.factorial(q - c));
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
        	Node x = QuadUtils.getNode(source, i);
        	Node y = QuadUtils.getNode(target, i);

        	// Skip non-variable components
        	if(!x.isVariable()) {
        		if(!x.equals(y)) {
        			result = null;
        			break;
        		} else {
        			continue;
        		}
        	}

            Var s = (Var)x;
            // TODO Make sure y is a variable
            Var t = (Var)y;

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