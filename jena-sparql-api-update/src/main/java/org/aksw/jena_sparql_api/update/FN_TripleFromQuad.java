package org.aksw.jena_sparql_api.update;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

public class FN_TripleFromQuad
    implements Function<Quad, Triple>
{
    public FN_TripleFromQuad() {
        super();
    }

    @Override
    public Triple apply(Quad quad) {
        Triple result = quad.asTriple();
        return result;
    }

    public static final FN_TripleFromQuad fn = new FN_TripleFromQuad();
}
