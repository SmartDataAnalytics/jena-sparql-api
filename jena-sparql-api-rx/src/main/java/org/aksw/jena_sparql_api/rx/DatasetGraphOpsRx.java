package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import io.reactivex.rxjava3.core.FlowableTransformer;

public class DatasetGraphOpsRx {
    public static FlowableTransformer<Quad, Entry<Node, List<Quad>>> groupToList()
    {
        return FlowableOperatorSequentialGroupBy.<Quad, Node, List<Quad>>create(
                Quad::getGraph,
                graph -> new ArrayList<>(),
                (list, item) -> list.add(item)
           ).transformer();
    }

    public static FlowableTransformer<Quad, Entry<Node, DatasetGraph>> groupConsecutiveQuadsRaw(
            Function<Quad, Node> grouper,
            Supplier<? extends DatasetGraph> graphSupplier) {

        return FlowableOperatorSequentialGroupBy.<Quad, Node, DatasetGraph>create(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                DatasetGraph::add).transformer();

//        return upstream ->
//            upstream
//                .lift(new OperatorOrderedGroupBy<Triple, Node, Graph>(
//                        grouper::apply,
//                        groupKey -> graphSupplier.get(),
//                        Graph::add));
    }


//    public static FlowableTransformer<Quad, Entry<Node, DatasetGraph>> graphsFromConsecutiveSubjectsRaw() {
//        return graphsFromConsecutiveSubjectsRaw(DatasetGraphFactory::create);
//    }
//
//    public static FlowableTransformer<Quad, Entry<Node, DatasetGraph>> graphsFromConsecutiveSubjectsRaw(Supplier<DatasetGraph> graphSupplier) {
//        return groupConsecutiveQuadsRaw(Quad::getGraph, graphSupplier);
//    }
//
//    public static FlowableTransformer<Quad, DatasetGraph> graphsFromConsecutiveSubjects() {
//        return graphsFromConsecutiveSubjects(DatasetGraphFactory::create);
//    }
//
//    public static FlowableTransformer<Quad, DatasetGraph> graphsFromConsecutiveSubjects(Supplier<DatasetGraph> graphSupplier) {
//        return graphsFromConsecutiveQuads(Quad::getGraph, graphSupplier);
//    }
//

//    public static FlowableTransformer<Quad, Entry<Node, DatasetGraph>> graphsFromConsecutiveGraphsRaw() {
//        return graphsFromConsecutiveSubjectsRaw(DatasetGraphFactory::create);
//    }
//
//    public static FlowableTransformer<Quad, Entry<Node, DatasetGraph>> graphsFromConsecutiveGraphsRaw(Supplier<DatasetGraph> graphSupplier) {
//        return groupConsecutiveQuadsRaw(Quad::getGraph, graphSupplier);
//    }
//
//    public static FlowableTransformer<Quad, DatasetGraph> graphsFromConsecutiveGraphs() {
//        return graphsFromConsecutiveSubjects(DatasetGraphFactory::create);
//    }
//
//    public static FlowableTransformer<Quad, DatasetGraph> graphsFromConsecutiveGraphs(Supplier<DatasetGraph> graphSupplier) {
//        return graphsFromConsecutiveQuads(Quad::getGraph, graphSupplier);
//    }



    public static FlowableTransformer<Quad, DatasetGraph> graphsFromConsecutiveQuads(
            Function<Quad, Node> grouper,
            Supplier<DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(groupConsecutiveQuadsRaw(grouper, graphSupplier))
                .map(Entry::getValue);
    }

    public static FlowableTransformer<Quad, Dataset> datasetsFromConsecutiveQuads(
            Function<Quad, Node> grouper,
            Supplier<DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(groupConsecutiveQuadsRaw(grouper, graphSupplier))
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }

    public static FlowableTransformer<Quad, Dataset> datasetsFromConsecutiveQuads(
            Supplier<? extends DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(groupConsecutiveQuadsRaw(Quad::getGraph, graphSupplier))
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }

}
