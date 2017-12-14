package org.aksw.jena_sparql_api.query_containment.index;

import org.aksw.commons.collections.trees.TreeNode;


/**
 * Internal datastructure used by the {@link QueryContainmentIndexImpl}
 * 
 * @author raven Oct 13, 2017
 *
 * @param <K>
 * @param <A>
 * @param <X>
 * @param <G>
 */
public class LeafInfo<K, A, X, Y, G> {
	protected K viewKey;
	protected X metaGraph;
	protected Y leafData;
	protected G graph;
	protected TreeNode<A> node;
	
	public LeafInfo(K viewKey, X metaGraph, Y leafData, G graph, TreeNode<A> node) {
		super();
		this.viewKey = viewKey;
		this.metaGraph = metaGraph;
		this.leafData = leafData;
		this.graph = graph;
		this.node = node;
	}

	public K getViewKey() {
		return viewKey;
	}

	public X getMetaGraph() {
		return metaGraph;
	}
	
	public Y getLeafdata() {
		return leafData;
	}

	public G getGraph() {
		return graph;
	}

	public TreeNode<A> getNode() {
		return node;
	}
	
}
