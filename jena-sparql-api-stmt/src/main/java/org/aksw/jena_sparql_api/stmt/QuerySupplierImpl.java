package org.aksw.jena_sparql_api.stmt;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.utils.PrologueUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Prologue;


public class QuerySupplierImpl
    implements Supplier<Query>
{
    protected Prologue prologue;
    protected String baseURI;

    public QuerySupplierImpl() {
        this(null);
    }

    public QuerySupplierImpl(Prologue prologue) {
        this(prologue, null);
    }

    public QuerySupplierImpl(Prologue prologue, String baseURI) {
        super();
        this.prologue = prologue;
        this.baseURI = baseURI;
    }


    @Override
    public Query get() {
        Query result = new Query();

        if (prologue != null) {
            PrologueUtils.copy(result.getPrologue(), prologue);
        }

        if (baseURI != null) {
            // result.setBaseURI(baseURI);
            result.setBase(result.getResolver().resolve(baseURI));
        }

        return result;
    }

}
