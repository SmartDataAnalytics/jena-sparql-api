package org.aksw.jena_sparql_api.utils;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.collections.diff.Diff;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;

public class DatasetGraphUtils {
    public static void addAll(DatasetGraph target, Node g, Graph source) {
        Iterator<Triple> it = source.find();
        while(it.hasNext()) {
            Triple t = it.next();
            target.add(new Quad(g, t));
        }
    }

	
    public static void addAll(DatasetGraph target, DatasetGraph source) {
            Iterator<Quad> it = source.find();
            addAll(target, it);
    }

    public static void addAll(DatasetGraph datasetGraph, Iterable<? extends Quad> items) {
        addAll(datasetGraph, items.iterator());
    }

    public static void addAll(DatasetGraph datasetGraph, Iterator<? extends Quad> it) {
        while(it.hasNext()) {
            Quad q = it.next();
            datasetGraph.add(q);
        }
    }

    public static DatasetGraph clone(DatasetGraph datasetGraph) {
        Iterator<Quad> it = datasetGraph.find();
        DatasetGraph clone = DatasetGraphFactory.createGeneral();
        addAll(clone, it);

        return clone;
    }

    /**
     * Merges two mappings of Node-&gt;DatasetGraph
     * Maybe this util class is not exactly the best place where to put it
     *
     * @param result
     * @param other
     * @return
     */
    public static Map<Node, DatasetGraph> mergeInPlace(Map<Node, DatasetGraph> result, Map<Node, DatasetGraph> other) {
        for(Entry<Node, DatasetGraph> entry : other.entrySet()) {
            Node node = entry.getKey();
            DatasetGraph otherGraph = entry.getValue();
            DatasetGraph graph = result.get(node);
            if(graph == null) {
                graph = DatasetGraphFactory.createGeneral();
                result.put(node, graph);
            }

            DatasetGraphUtils.addAll(graph, otherGraph);
        }

        return result;
    }


    public static Diff<DatasetGraph> wrapDiffDatasetGraph(Diff<? extends Iterable<? extends Quad>> diff) {
        DatasetGraph added = DatasetGraphFactory.createGeneral();
        DatasetGraph removed = DatasetGraphFactory.createGeneral();

        DatasetGraphUtils.addAll(added, diff.getAdded());
        DatasetGraphUtils.addAll(removed, diff.getRemoved());


        Diff<DatasetGraph> result = new Diff<DatasetGraph>(added, removed, null);
        return result;
    }

    public static void write(PrintStream out, DatasetGraph dg) {
        Dataset ds = DatasetFactory.wrap(dg);


        Model dm = ds.getDefaultModel();
        if(!dm.isEmpty()) {
            out.println("Begin of Default model -----------------------");
            dm.write(out, "TURTLE");
            out.println("End of Default model -----------------------");
        }
        Iterator<String> it = ds.listNames();
        while(it.hasNext()) {
            String name = it.next();
            Model model = ds.getNamedModel(name);
            System.out.println("Begin of " + name + " -----------------------");
            model.write(out, "TURTLE");
            System.out.println("End of " + name + " -----------------------");
        }

    }

}
