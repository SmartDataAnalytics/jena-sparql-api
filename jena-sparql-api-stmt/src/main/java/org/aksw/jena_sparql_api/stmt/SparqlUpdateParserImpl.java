package org.aksw.jena_sparql_api.stmt;

import java.util.function.Supplier;

import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;


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
        SparqlUpdateParserImpl result = create(config.getSyntax(), config.getPrologue(), config.getBaseURI(), config.getSharedPrefixes());
        return result;
    }

    public static SparqlUpdateParserImpl create() {
        SparqlUpdateParserImpl result = create(Syntax.syntaxARQ);
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax) {
        Prologue prologue = new Prologue(new PrefixMappingImpl(), IRIxResolver.create().noBase().allowRelative(true).build());
        //prologue.setBaseURI(IRIResolver.createNoResolve());

        SparqlUpdateParserImpl result = create(syntax, prologue);
        return result;
    }

    public static SparqlUpdateParserImpl create(Syntax syntax, Prologue prologue) {
        return create(syntax, prologue, "", null);
    }

    public static SparqlUpdateParserImpl create(Syntax syntax, Prologue prologue, String baseURI, PrefixMapping sharedPrefixes) {
        Supplier<UpdateRequest> updateSupplier = new UpdateSupplierImpl(prologue, baseURI, sharedPrefixes);

        SparqlUpdateParserImpl result = new SparqlUpdateParserImpl(updateSupplier, syntax, null);
        return result;
    }

    /** Return a parser without base url that retains relative IRIs */
    public static SparqlUpdateParserImpl createAsGiven() {
        return create(SparqlParserConfig.newInstance().parseAsGiven().applyDefaults());
    }

}

