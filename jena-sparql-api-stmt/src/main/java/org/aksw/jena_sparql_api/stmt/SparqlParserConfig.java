package org.aksw.jena_sparql_api.stmt;

import org.aksw.jena_sparql_api.util.iri.IRIxResolverUtils;
import org.aksw.jena_sparql_api.util.iri.PrologueUtils;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Prologue;

public class SparqlParserConfig
    implements Cloneable
{
    protected Syntax syntax;
    protected Prologue prologue;
    protected String baseURI;
    // It may be better to support prefix optimization as a post processor
//    protected boolean optimizePrefixes;

    public SparqlParserConfig clone() {
        SparqlParserConfig result = new SparqlParserConfig(syntax, prologue.copy());
        return result;
    }

    public SparqlParserConfig() {
        super();
    }

    public SparqlParserConfig(Syntax syntax, Prologue prologue) {
        super();
        this.syntax = syntax;
        this.prologue = prologue;
    }

    public SparqlParserConfig(Syntax syntax, Prologue prologue, String baseURI) {
        super();
        this.syntax = syntax;
        this.prologue = prologue;
        this.baseURI = baseURI;
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public SparqlParserConfig setSyntax(Syntax syntax) {
        this.syntax = syntax;
        return this;
    }

    public Prologue getPrologue() {
        return prologue;
    }

    public SparqlParserConfig setPrologue(Prologue prologue) {
        this.prologue = prologue;
        return this;
    }

    public SparqlParserConfig setIrixResolver(IRIxResolver resolver) {
        if(prologue == null) {
            prologue = new Prologue(new PrefixMappingImpl(), resolver);
        } else {
            PrologueUtils.setResolver(prologue, resolver);
        }
        return this;
    }

    public SparqlParserConfig setPrefixMapping(PrefixMapping pm) {
        if(prologue == null) {
            prologue = new Prologue(pm, newDefaultIrixResolver());
        } else {
            prologue.setPrefixMapping(pm);
        }
        return this;
    }

    /**
     * Parse sparql statements as given - without resolving relative IRIs
     * Sets the base URL to an empty string and configures the iri resolver without a base.
     */
    public SparqlParserConfig parseAsGiven() {
        setIrixResolverAsGiven();
        setBaseURI("");
        return this;
    }

    public SparqlParserConfig setIrixResolverAsGiven() {
        return setIrixResolver(newDefaultIrixResolver());
    }

    protected IRIxResolver newDefaultIrixResolver() {
        return IRIxResolverUtils.newIRIxResolverAsGiven();
    }

//    public SparqlParserConfig setBaseURI() {
//
//    }

    public PrefixMapping getPrefixMapping() {
        PrefixMapping result = prologue == null ? null : prologue.getPrefixMapping();
        return result;
    }

    public static SparqlParserConfig newInstance() {
        SparqlParserConfig result = new SparqlParserConfig();
        return result;
    }

//	public boolean optimizePrefixes() {
//		return optimizePrefixes;
//	}
//
//	public SparqlParserConfig optimizePrefixes(boolean optimizePrefixes) {
//		this.optimizePrefixes = optimizePrefixes;
//		return this;
//	}

    /** The default baseURI by which to parameterize fresh SPARQL statements */
    public String getBaseURI() {
        return baseURI;
    }

    public SparqlParserConfig setBaseURI(String baseURI) {
        this.baseURI = baseURI;
        return this;
    }

    public SparqlParserConfig applyDefaults() {
        if(syntax == null) {
            syntax = Syntax.syntaxARQ;
        }

        if(prologue == null) {
            prologue = new Prologue(PrefixMapping.Extended);
        }

        if(prologue.getResolver() == null) {
            // Avoid creation of another prologue instance because it may be referenced from elsewhere

//        	prologue = new Prologue(
//        			prologue.getPrefixMapping(),
//        			IRIxResolver.create().resolve(false).allowRelative(true).build());

            PrologueUtils.setResolver(prologue, newDefaultIrixResolver());
            baseURI = "";

            // prologue.setResolver(IRIxResolver.create().resolve(false).allowRelative(true).build());
        }

        return this;
    }

}
