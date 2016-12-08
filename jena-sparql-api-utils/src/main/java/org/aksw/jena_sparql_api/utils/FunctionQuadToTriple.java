package org.aksw.jena_sparql_api.utils;

import com.google.common.base.Function;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

public class FunctionQuadToTriple
    implements Function<Quad, Triple>
{
    @Override
    public Triple apply(Quad quad) {
        Triple result = quad.asTriple();
        return result;
    }

    public static final FunctionQuadToTriple fn = new FunctionQuadToTriple();
}
