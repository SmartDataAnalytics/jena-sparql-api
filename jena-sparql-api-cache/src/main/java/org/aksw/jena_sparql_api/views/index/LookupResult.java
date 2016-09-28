package org.aksw.jena_sparql_api.views.index;

import org.aksw.jena_sparql_api.view_matcher.OpVarMap;

public class LookupResult<V> {
	protected MyEntry<V> entry;
	protected OpVarMap opVarMap;

	public LookupResult(MyEntry<V> entry, OpVarMap opVarMap) {
		super();
		this.entry = entry;
		this.opVarMap = opVarMap;
	}

	public MyEntry<V> getEntry() {
		return entry;
	}

	public OpVarMap getOpVarMap() {
		return opVarMap;
	}

	@Override
	public String toString() {
		return "LookupResult [" + entry + ", " + opVarMap + "]";
	}
}