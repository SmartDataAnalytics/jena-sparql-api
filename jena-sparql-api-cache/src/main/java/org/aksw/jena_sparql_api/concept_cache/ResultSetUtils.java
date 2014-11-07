package org.aksw.jena_sparql_api.concept_cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

class ResultSetUtils {

    public static Multimap<List<Node>, Binding> index(ResultSet rs, List<Var> vars) {
        Multimap<List<Node>, Binding> result = LinkedListMultimap.create();

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();

            List<Node> key = new ArrayList<Node>(vars.size());
            for(Var var : vars) {
                Node node = binding.get(var);
                key.add(node);
            }

            result.put(key, binding);
        }

        return result;
    }

    public static ResultSet join(ResultSet a, ResultSet b) {
        //Set<String> aVarNames = new HashSet<String>(a.getResultVars());
        List<String> aVarNames = a.getResultVars();

        Set<String> joinVarNames = new HashSet<String>(aVarNames);
        joinVarNames.retainAll(b.getResultVars());

        List<String> bVarsOnly = new ArrayList<String>(b.getResultVars());
        bVarsOnly.removeAll(joinVarNames);

        List<String> allVars = new ArrayList<String>(aVarNames);
        allVars.addAll(bVarsOnly);


        List<Var> joinVars = VarUtils.toList(joinVarNames);

        Multimap<List<Node>, Binding> ma = index(a, joinVars);
        Multimap<List<Node>, Binding> mb = index(b, joinVars);

        Set<List<Node>> keys = new HashSet<List<Node>>(ma.keySet());
        keys.retainAll(mb.keySet());

        // Clean up unused keys
        ma.keySet().retainAll(keys);
        mb.keySet().retainAll(keys);

        Iterator<Binding> joinIterator = new IteratorJoin<List<Node>>(keys.iterator(), ma, mb);

        QueryIterator queryIter = new QueryIterPlainWrapper(joinIterator);

        ResultSet result = ResultSetFactory.create(queryIter, allVars);
        return result;
    }


    public static ResultSet project(ResultSet rs, Iterable<Var> vars, boolean uniq) {

        Collection<Binding> newBindings = uniq
            ? new HashSet<Binding>()
            : new ArrayList<Binding>()
            ;

        while(rs.hasNext()) {
            Binding o = rs.nextBinding();

            BindingHashMap n = new BindingHashMap();

            for(Var var : vars) {
                Node node = o.get(var);
                n.add(var, node);;
            }

            newBindings.add(n);
        }

        QueryIterator queryIter = new QueryIterPlainWrapper(newBindings.iterator());

        List<String> varNames = org.aksw.jena_sparql_api.utils.VarUtils.getVarNames(vars);
        ResultSet result = ResultSetFactory.create(queryIter, varNames);

        return result;
    }

}