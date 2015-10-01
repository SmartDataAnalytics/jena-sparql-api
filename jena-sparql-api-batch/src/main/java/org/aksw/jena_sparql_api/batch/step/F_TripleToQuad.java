package org.aksw.jena_sparql_api.batch.step;

import com.google.common.base.Function;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

public class F_TripleToQuad
    implements Function<Triple, Quad>
{
    @Override
    public Quad apply(Triple triple) {
        Quad result = new Quad(Quad.defaultGraphNodeGenerated, triple);
        return result;
    }


    public static final F_TripleToQuad fn = new F_TripleToQuad();
}
