package org.aksw.jena_sparql_api.views.index;

import java.util.Map;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.jena.jgrapht.LabeledEdgeImpl;
import org.aksw.jena_sparql_api.algebra.utils.FilteredQuad;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;

public class IndexFilteredTriplePattern<K>
    implements PatternIndex<K, FilteredQuad, Map<Var, Var>>
{
    //protected Map<K, Node> keyToNode;

    // Map keys to internal keys (which are nodes)
    protected ReversibleMap<K, Node> keyToNode;
    protected Graph<Node, LabeledEdge<Node, Node>> graph;

    public IndexFilteredTriplePattern() {
//        this.graph = new SimpleGraph<>((v, e) -> new LabeledEdgeImpl<>(v, e, null));
        this.graph = new SimpleGraph<>(null, () -> new LabeledEdgeImpl<>(), false);
        this.keyToNode = new ReversibleMapImpl<>();
    }

    @Override
    public FilteredQuad getPattern(K key) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public K allocate(FilteredQuad pattern) {



        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void put(K key, FilteredQuad pattern) {



    }

    @Override
    public Map<K, Map<Var, Var>> lookup(FilteredQuad prototype) {
//        Quad quad = prototype.getQuad();
//        Set<Set<Expr>> dnf = prototype.getExpr().getDnf();
//
//        dnf.stream().forEach(cnf -> {
//            QueryToGraph.equalExprsToGraph(graph, dnf);
//            Node quadNode = QueryToGraph.addQuad(graph, quad);
//
//
//        });

        return null;
    }

    @Override
    public void removeKey(Object key) {
        // TODO Auto-generated method stub

    }

}
