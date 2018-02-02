package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Supplier;
import org.apache.jena.query.Query;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;

public class QuerySupplierImpl
    implements Supplier<Query>
{
    protected Prologue prologue;

    public QuerySupplierImpl() {
        this(null);
    }

    public QuerySupplierImpl(Prologue prologue) {
        super();
        this.prologue = prologue;
    }


    @Override
    public Query get() {
        Query result = new Query();

        if(prologue != null) {
            result.setBaseURI(prologue.getBaseURI());
//            PrefixMappingImpl tmp = new PrefixMappingImpl();
//            // Note: Query parsing may modify the prefixes, hence create a copy of the prefix mappings
//            tmp.setNsPrefixes(prologue.getPrefixMapping());
            result.getPrefixMapping().setNsPrefixes(prologue.getPrefixMapping());
        }

        return result;
    }


}
