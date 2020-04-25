package org.aksw.jena_sparql_api.rx;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import io.reactivex.FlowableTransformer;

public class GraphOpsRx {
    public static FlowableTransformer<Triple, Entry<Node, Graph>> groupConsecutiveTriplesRaw(
            Function<Triple, Node> grouper,
            Supplier<Graph> graphSupplier) {

        return new OperatorOrderedGroupBy<Triple, Node, Graph>(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                Graph::add).transformer();
//        return upstream ->
//            upstream
//                .lift(new OperatorOrderedGroupBy<Triple, Node, Graph>(
//                        grouper::apply,
//                        groupKey -> graphSupplier.get(),
//                        Graph::add));
    }

    public static FlowableTransformer<Triple, Graph> graphFromConsecutiveTriples(
            Function<Triple, Node> grouper,
            Supplier<Graph> graphSupplier) {
        return upstream -> upstream
                .compose(groupConsecutiveTriplesRaw(grouper, graphSupplier))
                .map(Entry::getValue);

//        return upstream ->
//            upstream
//                .lift(new OperatorOrderedGroupBy<Triple, Node, Graph>(
//                        grouper::apply,
//                        groupKey -> graphSupplier.get(),
//                        Graph::add))
//                .map(Entry::getValue);
    }
}
