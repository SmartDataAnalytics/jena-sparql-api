package org.aksw.jena_sparql_api.stmt;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.util.iri.PrefixMappingTrie;
import org.aksw.jena_sparql_api.util.iri.PrologueUtils;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.util.PrefixMapping2;


public class QuerySupplierImpl
    extends StmtSupplierBase
    implements Supplier<Query>
{
    public QuerySupplierImpl() {
        this(null);
    }

    public QuerySupplierImpl(Prologue prologue) {
        this(prologue, null);
    }

    public QuerySupplierImpl(Prologue prologue, String baseURI) {
        this(prologue, baseURI, null);
    }

    public QuerySupplierImpl(Prologue prologue, String baseURI, PrefixMapping sharedPrefixes) {
        super(prologue, baseURI, sharedPrefixes);
    }


    @Override
    public Query get() {
        PrefixMapping prefixMapping = new PrefixMappingTrie();
        if (sharedPrefixes != null) {
            prefixMapping = new PrefixMapping2(sharedPrefixes, prefixMapping);
        }

        Query result = new Query();
        result.setPrefixMapping(prefixMapping);

        if (prologue != null) {
            PrologueUtils.configure(result, prologue, baseURI);
        }

        return result;
    }

}
