package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.shape.syntax.Element;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitor;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitorSparql;

public class Algebra {
    protected static final ElementVisitor<Op> elementCompiler = new ElementVisitorSparql();

    protected static final OpVisitor<Concept> opCompiler = new OpVisitorSparql();

    public static Op compile(Element e) {
        Op result = e.accept(elementCompiler);
        return result;
    }

    public static Concept toConcept(Op op) {
        Concept result = op.accept(opCompiler);
        return result;
    }
}
