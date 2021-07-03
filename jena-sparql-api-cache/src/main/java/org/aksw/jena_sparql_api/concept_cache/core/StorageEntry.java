package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.commons.rx.range.RangedSupplier;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.apache.jena.sparql.engine.binding.Binding;

public class StorageEntry {
	//public StorageEntry(RangedSupplierLazyLoadingListCache<Binding> storage, VarInfo varInfo) {
	public StorageEntry(RangedSupplier<Long, Binding> storage, VarInfo varInfo) {
		super();
		this.storage = storage;
		this.varInfo = varInfo;
	}

	//public RangedSupplierLazyLoadingListCache<Binding> storage;
	public RangedSupplier<Long, Binding> storage;
	public VarInfo varInfo;

	@Override
	public String toString() {
		return "StorageEntry [storage=" + storage + ", varInfo=" + varInfo + "]";
	}


}