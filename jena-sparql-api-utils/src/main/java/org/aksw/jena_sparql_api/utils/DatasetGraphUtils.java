package org.aksw.jena_sparql_api.utils;

import java.util.Iterator;

import org.aksw.commons.collections.diff.Diff;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Quad;

public class DatasetGraphUtils {
	public static void addAll(DatasetGraph target, DatasetGraph source) {
			Iterator<Quad> it = source.find();
			addAll(target, it);
	}

    public static void addAll(DatasetGraph datasetGraph, Iterable<Quad> items) {
    	addAll(datasetGraph, items.iterator());
    }

    public static void addAll(DatasetGraph datasetGraph, Iterator<Quad> it) {
		while(it.hasNext()) {
			Quad q = it.next();
			datasetGraph.add(q);
		}
    }

    public static DatasetGraph clone(DatasetGraph datasetGraph) {
		Iterator<Quad> it = datasetGraph.find();
		DatasetGraph clone = DatasetGraphFactory.createMem();
		addAll(clone, it);

		return clone;
    }

    public static Diff<DatasetGraph> wrapDiffDatasetGraph(Diff<? extends Iterable<? extends Quad>> diff) {
    	DatasetGraph added = DatasetGraphFactory.createMem();
    	DatasetGraph removed = DatasetGraphFactory.createMem();

    	Diff<DatasetGraph> result = new Diff<DatasetGraph>(added, removed, null);
    	return result;
    }

}
