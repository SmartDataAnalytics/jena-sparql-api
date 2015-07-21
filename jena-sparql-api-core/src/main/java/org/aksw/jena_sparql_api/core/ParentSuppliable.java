package org.aksw.jena_sparql_api.core;

import com.google.common.base.Supplier;

public interface ParentSuppliable<P> {
    void setParentSupplier(Supplier<P> parentSupplier);
}
