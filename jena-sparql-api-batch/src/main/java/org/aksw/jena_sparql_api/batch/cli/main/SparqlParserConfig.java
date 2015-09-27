package org.aksw.jena_sparql_api.batch.cli.main;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;

public class SparqlParserConfig {
    protected Syntax syntax;
    protected Prologue prologue;

    public SparqlParserConfig(Syntax syntax, Prologue prologue) {
        super();
        this.syntax = syntax;
        this.prologue = prologue;
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public Prologue getPrologue() {
        return prologue;
    }
}
