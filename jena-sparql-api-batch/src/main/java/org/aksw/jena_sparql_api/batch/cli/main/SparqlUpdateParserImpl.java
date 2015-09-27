package org.aksw.jena_sparql_api.batch.cli.main;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.update.UpdateRequest;

public class SparqlUpdateParserImpl
    implements SparqlUpdateParser
{
    protected Supplier<UpdateRequest> updateRequestSupplier;
    protected Syntax syntax;
    protected String baseURI;


    public SparqlUpdateParserImpl(Supplier<UpdateRequest> updateRequestSupplier, Syntax syntax, String baseURI) {
        super();
        this.updateRequestSupplier = updateRequestSupplier;
        this.syntax = syntax;
        this.baseURI = baseURI;
    }

    @Override
    public UpdateRequest apply(String queryString) {
        UpdateRequest result = updateRequestSupplier.get();
        QueryFactory.parse(result, queryString, baseURI, syntax);
        return result;
    }

    public static SparqlUpdateParserImpl create(SparqlParserConfig config) {
        SparqlUpdateParserImpl result = create(config.getSyntax(), config.getPrologue());
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax) {
        SparqlUpdateParserImpl result = create(syntax, null);
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax, Prologue prologue) {
        Supplier<Query> querySupplier= new QuerySupplierImpl(prologue);

        SparqlUpdateParserImpl result = new SparqlUpdateParserImpl(querySupplier, syntax, null);
        return result;
    }

}
