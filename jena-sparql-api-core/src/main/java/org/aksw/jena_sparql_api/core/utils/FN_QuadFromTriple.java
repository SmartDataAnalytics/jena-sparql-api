package org.aksw.jena_sparql_api.core.utils;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

public class FN_QuadFromTriple
    implements Function<Triple, Quad>
{
    private Node g;

    public FN_QuadFromTriple(Node g) {
        super();
        this.g = g;
    }

    @Override
    public Quad apply(Triple triple) {
        Quad result = Quad.create(g, triple);
        return result;
    }

    public static FN_QuadFromTriple create(Node g) {
        FN_QuadFromTriple result = new FN_QuadFromTriple(g);
        return result;
    }

    public static final FN_QuadFromTriple fnDefaultGraphNodeGenerated = new FN_QuadFromTriple(Quad.defaultGraphNodeGenerated);
}
