package org.aksw.jena_sparql_api.concept_cache.core;

import org.aksw.jena_sparql_api.util.collection.RangedSupplierLazyLoadingListCache;
import org.apache.jena.sparql.engine.binding.Binding;

public class StorageEntry {
	public StorageEntry(RangedSupplierLazyLoadingListCache<Binding> storage, VarInfo varInfo) {
		super();
		this.storage = storage;
		this.varInfo = varInfo;
	}

	public RangedSupplierLazyLoadingListCache<Binding> storage;
	public VarInfo varInfo;
}