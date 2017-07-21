package org.aksw.jena_sparql_api.iso.index;

import java.util.function.Supplier;

public class SetOpsImpl<S> {
    protected Supplier<S> create;

    public SetOpsImpl(Supplier<S> create) {
        this.create = create;
    }
    
    

}
