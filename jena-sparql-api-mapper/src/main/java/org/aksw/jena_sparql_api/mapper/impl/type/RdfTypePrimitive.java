package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

import com.hp.hpl.jena.graph.Graph;

public abstract class RdfTypePrimitive
    extends RdfTypeBase
{
    public RdfTypePrimitive(RdfTypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public void writeGraph(Graph out, Object obj) {
    }
}
