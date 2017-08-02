package org.aksw.jena_sparql_api.iso.index;

import java.util.Set;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.jgrapht.transform.GraphVar;
import org.aksw.jena_sparql_api.jgrapht.transform.GraphVarImpl;
import org.aksw.jena_sparql_api.utils.SetGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;

import com.google.common.collect.Sets;

public class SetOpsGraph<G> {
    protected Supplier<G> newSet;

    public static GraphVar difference(Graph ag, Graph bg) {
        Set<Triple> as = new SetGraph(ag);
        Set<Triple> bs = new SetGraph(bg);
        Set<Triple> c = Sets.difference(as, bs);

        GraphVar result = new GraphVarImpl();
        GraphUtil.add(result, c.iterator());

        return result;
    }

    public static GraphVar intersection(Graph ag, Graph bg) {
        Set<Triple> as = new SetGraph(ag);
        Set<Triple> bs = new SetGraph(bg);
        Set<Triple> c = Sets.intersection(as, bs);

        GraphVar result = new GraphVarImpl();
        GraphUtil.add(result, c.iterator());

        return result;
    }


}
