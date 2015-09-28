package org.aksw.jena_sparql_api.batch.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.diff.Diff;
import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.modifier.Modifier;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.SetDatasetGraph;
import org.springframework.batch.item.ItemProcessor;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;

public class ItemProcessorModifierDatasetGraphDiff
    implements ItemProcessor<Entry<? extends Node, ? extends DatasetGraph>, Entry<Node, Diff<DatasetGraph>>>
{
    private Modifier<? super DatasetGraph> modifier;

    public ItemProcessorModifierDatasetGraphDiff(Modifier<? super DatasetGraph> modifier) {
        this.modifier = modifier;
    }



    public static Diff<DatasetGraph> computeDiff(DatasetGraph base, Modifier<? super DatasetGraph> modifier) {
		DatasetGraph clone = DatasetGraphUtils.clone(base);

		modifier.apply(clone);

		Set<Quad> baseQuads = SetDatasetGraph.wrap(base);
		Set<Quad> cloneQuads = SetDatasetGraph.wrap(clone);

		//Diff<Set<Quad>>
		Diff<Set<Quad>> tmp = createDiff(cloneQuads, baseQuads);
		Diff<DatasetGraph> result = DatasetGraphUtils.wrapDiffDatasetGraph(tmp);

		return result;
    }

    @Override
    public Entry<Node, Diff<DatasetGraph>> process(Entry<? extends Node, ? extends DatasetGraph> item) {
    	Node node = item.getKey();
		DatasetGraph base = item.getValue(); //base.asDatasetGraph();

		Diff<DatasetGraph> diff = computeDiff(base, modifier);
		Entry<Node, Diff<DatasetGraph>> result = Pair.create(node, diff);

		return result;
	}

	public static <T> Diff<Set<T>> createDiff(Collection<T> after, Collection<T> before) {
		Set<T> x = SetUtils.asSet(after);
		Set<T> y = SetUtils.asSet(before);

		Set<T> added = new HashSet<T>(Sets.difference(x, y));
		Set<T> removed = new HashSet<T>(Sets.difference(y, x));
		Diff<Set<T>> result = Diff.create(added, removed);
		return result;
	}


    public Entry<Node, Diff<Set<Quad>>> process2(Entry<Node, DatasetGraph> item)
            throws Exception {
        DatasetGraph m = item.getValue();

        modifier.apply(m);
        return null;
    }
}
