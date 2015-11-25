package org.aksw.jena_sparql_api.batch.step;

import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class F_QuadToBinding
    implements Function<Quad, Binding>
{
    @Override
    public Binding apply(Quad quad) {
        Binding result = QuadUtils.quadToBinding(quad);
        return result;
    }

    public static final F_QuadToBinding fn = new F_QuadToBinding();
}
