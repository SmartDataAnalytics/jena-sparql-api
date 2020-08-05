package org.aksw.jena_sparql_api.utils;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aksw.commons.collections.diff.ListDiff;
import org.aksw.jena_sparql_api.utils.model.QuerySolutionWithEquals;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;


public class ResultSetUtils {

    public static Multiset<QuerySolution> toMultisetQs(ResultSet rs) {
        Multiset<QuerySolution> result = HashMultiset.create();
        while(rs.hasNext()) {
            QuerySolution original = rs.next();

            QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

    public static Multiset<Binding> toMultiset(ResultSet rs) {
        Multiset<Binding> result = HashMultiset.create();
        while(rs.hasNext()) {
            Binding original = rs.nextBinding();

            Binding wrapped = original;
            //QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

    /**
     * Traverse the resultset in order, and write out the missing items on each side:
     * 1 2
     * ---
     * a a
     * b c
     * d d
     *
     * gives:
     * [c] [b]
     *
     * (1 lacks c, 2 lacks b)
     *
     *
     * @param a
     * @param b
     * @return
     */
    public static ListDiff<Binding> compareOrdered(ResultSet a, ResultSet b) {
        ListDiff<Binding> result = new ListDiff<>();

        Binding x = null;
        Binding y = null;

        while(a.hasNext()) {
            if(!b.hasNext()) {
                while(a.hasNext()) {
                    result.getAdded().add(a.nextBinding());
                }
                return result;
            }

            //if((x == null && y == null) ||  x.equals(y)
            if(x == y || x.equals(y)) {
                x = a.nextBinding();
                y = b.nextBinding();
                continue;
            }

            String sx = x.toString();
            String sy = y.toString();

            if(sx.compareTo(sy) < 0) {
                result.getRemoved().add(x);
                x = a.nextBinding();
            } else {
                result.getAdded().add(y);
                y = b.nextBinding();
            }
        }

        while(b.hasNext()) {
            result.getRemoved().add(b.nextBinding());
        }

        return result;
    }

    public static ListDiff<Binding> compareUnordered(ResultSet a, ResultSet b) {
        ListDiff<Binding> result = new ListDiff<>();

        Multiset<Binding> x = toMultiset(a);
        Multiset<Binding> y = toMultiset(b);

        Multiset<Binding> common = HashMultiset.create(Multisets.intersection(x, y));

        y.removeAll(common);
        x.removeAll(common);

        result.getAdded().addAll(y);
        result.getRemoved().addAll(x);

        return result;
    }

    public static List<Var> getVars(ResultSet rs) {
        List<Var> result = VarUtils.toList(rs.getResultVars());
        return result;
    }

    public static Node getNextNode(ResultSet rs, Var v) {
        Node result = null;

        if (rs.hasNext()) {
            Binding binding = rs.nextBinding();
            result = binding.get(v);
        }
        return result;
    }

    public static Optional<Node> tryGetNextNode(ResultSet rs, Var v) {
        Node node = getNextNode(rs, v);
        Optional<Node> result = Optional.ofNullable(node);
        return result;
    }

    public static RDFNode getNextRDFNode(ResultSet rs, Var v) {
        RDFNode result = null;
        if (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String varName = v.getName();
            result = qs.get(varName);
        }
        return result;
    }

    public static Optional<RDFNode> tryGetNextRDFNode(ResultSet rs, Var v) {
        RDFNode node = getNextRDFNode(rs, v);
        Optional<RDFNode> result = Optional.ofNullable(node);
        return result;
    }

    public static Integer resultSetToInt(ResultSet rs, Var v) {
        Integer result = null;

        if (rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node node = binding.get(v);
            NodeValue nv = NodeValue.makeNode(node);
            result = nv.getInteger().intValue();

            // TODO Validate that the result actually is int.
            //result = node.getLiteral().
        }

        return result;
    }

//    public static Long resultSetToInt(ResultSet rs, Var v) {
//        Integer result = null;
//
//        if (rs.hasNext()) {
//            Binding binding = rs.nextBinding();
//
//            Node node = binding.get(v);
//            NodeValue nv = NodeValue.makeNode(node);
//            result = nv.getInteger().longValue();
//
//            // TODO Validate that the result actually is int.
//            //result = node.getLiteral().
//        }
//
//        return result;
//    }


    public static List<Node> resultSetToList(ResultSet rs, Var v) {
        List<Node> result = new ArrayList<Node>();
        while (rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node node = binding.get(v);
            result.add(node);
        }
        return result;
    }

    public static Map<Node, ResultSetPart> partition(ResultSet rs, Var var) {
        List<String> varNames = rs.getResultVars();
        Map<Node, ResultSetPart> result = new LinkedHashMap<Node, ResultSetPart>();

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node node = binding.get(var);

            ResultSetPart rsp = result.get(node);
            if(rsp == null) {

                rsp = new ResultSetPart(varNames);
                result.put(node, rsp);
            }

            rsp.getBindings().add(binding);
        }

        return result;
    }

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
            Binding n = BindingUtils.project(o, vars);

            newBindings.add(n);
        }

        ResultSet result = create2(vars, newBindings.iterator());

        return result;
    }

    public static ResultSet create(List<String> varNames, Iterator<Binding> bindingIt) {
        QueryIterator queryIter = new QueryIterPlainWrapper(bindingIt);

        ResultSet result = ResultSetFactory.create(queryIter, varNames);
        return result;
    }

    public static ResultSet create2(Iterable<Var> vars, Iterator<Binding> bindingIt) {
        List<String> varNames = org.aksw.jena_sparql_api.utils.VarUtils.getVarNames(vars);
        ResultSet result = create(varNames, bindingIt);
        return result;
    }

}