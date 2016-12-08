package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Supplier;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.UpdateRequest;

public class UpdateSupplierImpl
    implements Supplier<UpdateRequest>
{
    protected Prologue prologue;

    public UpdateSupplierImpl() {
        this(null);
    }

    public UpdateSupplierImpl(Prologue prologue) {
        super();
        this.prologue = prologue;
    }


    @Override
    public UpdateRequest get() {
        UpdateRequest result = new UpdateRequest();

        if(prologue != null) {
            result.setBaseURI(prologue.getResolver());
            result.setPrefixMapping(prologue.getPrefixMapping());
        }

        return result;
    }


}
