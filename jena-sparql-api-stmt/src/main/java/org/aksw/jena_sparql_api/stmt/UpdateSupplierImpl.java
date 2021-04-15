package org.aksw.jena_sparql_api.stmt;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.PrologueUtils;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
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
            // result.setBaseURI(prologue.getResolver());
        	PrologueUtils.setResolver(result, prologue.getResolver());
        	
            PrefixMapping copy = new PrefixMappingImpl();
            copy.setNsPrefixes(prologue.getPrefixMapping());

            result.setPrefixMapping(copy); //prologue.getPrefixMapping());
        }

        return result;
    }


}
