package org.aksw.jena_sparql_api.decision_tree.api;

public abstract class DtNodeBase<C, V, T>
	implements DtNode<C, V, T>
{
	public DtNodeBase(InnerNode<C, V, T> parent, V reachingValue) {
		super();
		this.parent = parent;
		this.reachingValue = reachingValue;
	}
	
	protected InnerNode<C, V, T> parent;
	protected V reachingValue;
	
	@Override
	public InnerNode<C, V, T> getParent() {
		return parent;
	}
	
	@Override
	public V getReachingValue() {
		return reachingValue;
	}
}