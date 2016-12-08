package org.aksw.jena_sparql_api.mapper;

import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

public class BindingMapperQuad
    implements BindingMapper<Quad>
{
    protected Quad quad;

    public BindingMapperQuad(Quad quad) {
        this.quad = quad;
    }

    @Override
    public Quad apply(Binding binding, Long rowNum) {
        Quad result = QuadUtils.copySubstitute(quad, binding);
        return result;
    }
}
