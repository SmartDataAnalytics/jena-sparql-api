package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

public abstract class RdfTypeBase
    implements RdfType
{
    protected RdfTypeFactory typeFactory;

    public RdfTypeBase(RdfTypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public RdfTypeFactory getTypeFactory() {
        return typeFactory;
    }
}
