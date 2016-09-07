package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;


public abstract class RdfTypeComplexBase
    extends RdfTypeBase
{
    public RdfTypeComplexBase(RdfTypeFactory typeFactory) {
        super(typeFactory);
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }
}
