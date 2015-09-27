package org.aksw.jena_sparql_api.stmt;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.update.UpdateFactory;
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
    public UpdateRequest apply(String updateString) {
        UpdateRequest result = updateRequestSupplier.get();
        UpdateFactory.parse(result, updateString, baseURI, syntax);
        return result;
    }

    public static SparqlUpdateParserImpl create(SparqlParserConfig config) {
        SparqlUpdateParserImpl result = create(config.getSyntax(), config.getPrologue());
        return result;
    }

    public static SparqlUpdateParserImpl create() {
        SparqlUpdateParserImpl result = create(Syntax.syntaxARQ);
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax) {
        SparqlUpdateParserImpl result = create(syntax, null);
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax, Prologue prologue) {
        Supplier<UpdateRequest> updateSupplier= new UpdateSupplierImpl(prologue);

        SparqlUpdateParserImpl result = new SparqlUpdateParserImpl(updateSupplier, syntax, null);
        return result;
    }

}
