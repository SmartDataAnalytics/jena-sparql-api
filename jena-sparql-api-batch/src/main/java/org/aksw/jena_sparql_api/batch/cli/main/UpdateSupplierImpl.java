package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.update.UpdateRequest;

public class UpdateSupplierImpl
    implements Supplier<UpdateRequest>
{
    protected Prologue prologue;

    public UpdateSupplierImpl() {
        this(null);
    }

    public UpdateSupplierImpl(Prologue prologue) {
        super();
    }


    @Override
    public UpdateRequest get() {
        UpdateRequest result = new UpdateRequest();

        if(prologue != null) {
            result.setBaseURI(prologue.getBaseURI());
            result.setPrefixMapping(prologue.getPrefixMapping());
        }

        return result;
    }


}
