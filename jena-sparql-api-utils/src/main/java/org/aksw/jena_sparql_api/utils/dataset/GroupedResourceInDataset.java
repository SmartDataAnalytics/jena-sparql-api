package org.aksw.jena_sparql_api.utils.dataset;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;

public class GroupedResourceInDataset {
	protected Dataset dataset;
	protected Set<GraphNameAndNode> graphNameAndNodes;
	
	public GroupedResourceInDataset(Dataset dataset) {
		this(dataset, new LinkedHashSet<>());
	}

	public GroupedResourceInDataset(Dataset dataset, Set<GraphNameAndNode> node) {
		super();
		this.dataset = dataset;
		this.graphNameAndNodes = node;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public Set<GraphNameAndNode> getGraphNameAndNodes() {
		return graphNameAndNodes;
	}

	@Override
	public String toString() {
		return "graphNameAndNodes=" + graphNameAndNodes + ", datasetSize=" + Streams.stream(dataset.asDatasetGraph().find()).count();
	}
}