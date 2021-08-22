package org.aksw.jena_sparql_api.stmt;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.PrologueUtils;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.UpdateRequest;

public class UpdateSupplierImpl
    implements Supplier<UpdateRequest>
{
    protected Prologue prologue;
    protected String baseURI;

    public UpdateSupplierImpl() {
        this(null);
    }

    public UpdateSupplierImpl(Prologue prologue) {
        this(prologue, null);
    }

    public UpdateSupplierImpl(Prologue prologue, String baseURI) {
        super();
        this.prologue = prologue;
        this.baseURI = baseURI;
    }


    @Override
    public UpdateRequest get() {
        UpdateRequest result = new UpdateRequest();

        if (prologue != null) {
            PrologueUtils.configure(result, prologue, baseURI);
        }

        return result;
    }


}
