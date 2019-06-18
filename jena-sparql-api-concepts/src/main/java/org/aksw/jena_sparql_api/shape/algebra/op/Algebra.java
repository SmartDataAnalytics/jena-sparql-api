package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.shape.syntax.Element;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitor;
import org.aksw.jena_sparql_api.shape.syntax.ElementVisitorSparql;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.sparql.core.Var;

public class Algebra {
    protected static final ElementVisitor<Op> elementCompiler = new ElementVisitorSparql();

    public static Op compile(Element e) {
        Op result = e.accept(elementCompiler);
        return result;
    }

    public static Concept toConcept(Op op) {
        Generator<Var> generator = VarGeneratorImpl2.create();
        OpVisitor<Concept> opCompiler = new OpVisitorSparql(generator);
        Concept result = op.accept(opCompiler);
        return result;
    }
}
