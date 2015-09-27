package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.Prologue;

public class QuerySupplierImpl
    implements Supplier<Query>
{
    protected Prologue prologue;

    public QuerySupplierImpl() {
        this(null);
    }

    public QuerySupplierImpl(Prologue prologue) {
        super();
    }


    @Override
    public Query get() {
        Query result = new Query();

        if(prologue != null) {
            result.setBaseURI(prologue.getBaseURI());
            result.setPrefixMapping(prologue.getPrefixMapping());
        }

        return result;
    }


}
