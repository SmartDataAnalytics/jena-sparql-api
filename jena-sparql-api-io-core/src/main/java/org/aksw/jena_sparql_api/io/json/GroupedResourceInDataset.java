package org.aksw.jena_sparql_api.io.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.query.Dataset;

public class GroupedResourceInDataset {
	protected Dataset dataset;
	protected List<GraphNameAndNode> graphNameAndNodes;
	
	public GroupedResourceInDataset(Dataset dataset) {
		this(dataset, new ArrayList<>());
	}

	public GroupedResourceInDataset(Dataset dataset, List<GraphNameAndNode> node) {
		super();
		this.dataset = dataset;
		this.graphNameAndNodes = node;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public List<GraphNameAndNode> getGraphNameAndNodes() {
		return graphNameAndNodes;
	}

	@Override
	public String toString() {
		return "graphNameAndNodes=" + graphNameAndNodes + ", datasetSize=" + Streams.stream(dataset.asDatasetGraph().find()).count();
	}
}