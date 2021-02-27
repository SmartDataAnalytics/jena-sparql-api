package org.aksw.jena_sparql_api.rx;

import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

import io.reactivex.rxjava3.core.FlowableTransformer;

public class GraphOpsRx {
    public static FlowableTransformer<Triple, Entry<Node, Graph>> groupConsecutiveTriplesRaw(
            Function<Triple, Node> grouper,
            Supplier<Graph> graphSupplier) {

        return FlowableOperatorSequentialGroupBy.<Triple, Node, Graph>create(
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

    public static FlowableTransformer<Triple, Entry<Node, Graph>> graphsFromConsecutiveSubjectsRaw() {
        return graphsFromConsecutiveSubjectsRaw(GraphFactory::createDefaultGraph);
    }

    public static FlowableTransformer<Triple, Entry<Node,Graph>> graphsFromConsecutiveSubjectsRaw(Supplier<Graph> graphSupplier) {
        return groupConsecutiveTriplesRaw(Triple::getSubject, graphSupplier);
    }

    public static FlowableTransformer<Triple, Graph> graphsFromConsecutiveSubjects() {
        return graphsFromConsecutiveSubjects(GraphFactory::createDefaultGraph);
    }

    public static FlowableTransformer<Triple, Graph> graphsFromConsecutiveSubjects(Supplier<Graph> graphSupplier) {
        return graphFromConsecutiveTriples(Triple::getSubject, graphSupplier);
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
