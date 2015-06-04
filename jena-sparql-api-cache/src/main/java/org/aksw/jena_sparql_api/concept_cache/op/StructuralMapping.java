package org.aksw.jena_sparql_api.concept_cache.op;

import java.io.IOException;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.experimental.equivalence.EquivalenceComparator;
import org.jgrapht.experimental.isomorphism.AdaptiveIsomorphismInspectorFactory;
import org.jgrapht.experimental.isomorphism.GraphIsomorphismInspector;
import org.jgrapht.experimental.isomorphism.IsomorphismRelation;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Function;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;


// For each node get a signature
//

//class PredicateLabelMatch<V, E, L>
//    implements Predicate<IsomorphismRelation<V, E>>
//{
//    public PredicateLabelMatch(Graph<V, E> a, Graph<V, E> b) {
//
//    }
//
//    @Override
//    public boolean apply(IsomorphismRelation<V, E> arg0) {
//        // test if the labels are equivalent
//    }
//
//}

class FunctionUtils {
    public static final Function<Object, String> classNameFn = new Function<Object, String>() {
        @Override
        public String apply(Object o) {
            String result = getClassLabel(o);
            return result;
        }
    };


    public static String getClassLabel(Object o) {
        String result = o == null ? "(null)" : o.getClass().getSimpleName();
        return result;
    }
}

class MyEquivalenceComparator<V, E, L>
    implements EquivalenceComparator<V, Graph<V, E>>
{
    private Function<? super V, L> labelFn;

    public MyEquivalenceComparator(Function<? super V, L> labelFn) {
        this.labelFn = labelFn;
    }

    @Override
    public boolean equivalenceCompare(V a, V b, Graph<V, E> ga, Graph<V, E> gb) {
        L la = labelFn.apply(a);
        L lb = labelFn.apply(b);

        boolean result = la.equals(lb);
        return result;
    }

    @Override
    public int equivalenceHashcode(V node, Graph<V, E> graph) {
        L l = labelFn.apply(node);
        return l.hashCode();
    }

    public static <V, E, L> MyEquivalenceComparator<V, E, L> create(Function<? super V, L> labelFn, Graph<V, E> graph) {
        MyEquivalenceComparator<V, E, L> result = new MyEquivalenceComparator<V, E, L>(labelFn);
        return result;
    }
}


public class StructuralMapping {
    public static void main(String[] args) throws IOException {
        String queryString = "Select ?s ?name  { ?s a <Person> . Optional { ?s <label> ?name . } }";
        Query query = QueryFactory.create(queryString);

        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);



        //Iterators.filter(itIso, predicate);

        DirectedAcyclicGraph<Op, DefaultEdge> graph = OpToDirectedGraph.convert(op);

        DirectedAcyclicGraph<Op, DefaultEdge> cacheGraph = graph;

        //EquivalenceIsomorphismInspector<Op, DefaultEdge> itIso = new Equival
        EquivalenceComparator<Op, Graph<Op, DefaultEdge>> vertexChecker =
                MyEquivalenceComparator.create(FunctionUtils.classNameFn, graph);
//Functions.
        GraphIsomorphismInspector<IsomorphismRelation<Op, DefaultEdge>> itIso =
                (GraphIsomorphismInspector<IsomorphismRelation<Op, DefaultEdge>>)
                AdaptiveIsomorphismInspectorFactory.createIsomorphismInspector(cacheGraph, graph, vertexChecker, null);

        int i = 0;
        while(itIso.hasNext()) {
            System.out.println("ISO " + i);
            System.out.println("===========");

            IsomorphismRelation<Op, DefaultEdge> iso = itIso.next();

            for(Op cacheVertex : cacheGraph.vertexSet()) {
                Op target = iso.getVertexCorrespondence(cacheVertex, true);
                String la = cacheVertex.getName();
                String lb = target.getName();
                System.out.println("correspondence: " + la + " --- " + lb);

            }



            ++i;
        }

        System.out.println("done");
        //Set<DefaultEdge> edges = graph.outgoingEdgesOf(op);

        Set<DefaultEdge> edges = graph.outgoingEdgesOf(OpUtils.getSubOps(op).get(0));

        for(DefaultEdge edge : edges) {
            System.out.println("child");
            Op target = graph.getEdgeTarget(edge);
            System.out.println(target);
        }


    }
}
