package org.aksw.jena_sparql_api.utils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;


public class ResultSetUtils {

    public static ExtendedIterator<Binding> toIteratorBinding(QueryExecution qe) {
        ResultSet rs = qe.execSelect();
        ExtendedIterator<Binding> result = toIteratorBinding(rs, qe);
        return result;
    }
    /**
     * This version returns an iterator capable of closing the corresponding query execution
     *
     * @param rs
     * @param qe
     * @return
     */
    public static ExtendedIterator<Binding> toIteratorBinding(ResultSet rs, QueryExecution qe) {
        Iterator<Binding> it = new IteratorResultSetBinding(rs);
        Closeable closeable = new CloseableQueryExecution(qe);
        Iterator<Binding> tmp = new IteratorClosable<Binding>(it, closeable);
        ExtendedIterator<Binding> result = WrappedIterator.create(tmp);

        return result;
    }

    public static Iterator<Binding> toIteratorBinding(ResultSet rs) {
        Iterator<Binding> result = new IteratorResultSetBinding(rs);
        return result;
    }

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