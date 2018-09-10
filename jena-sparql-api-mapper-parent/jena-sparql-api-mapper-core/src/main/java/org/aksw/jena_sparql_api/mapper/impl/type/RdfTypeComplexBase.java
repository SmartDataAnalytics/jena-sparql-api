package org.aksw.jena_sparql_api.mapper.impl.type;

public abstract class RdfTypeComplexBase
    extends RdfTypeBase
{
//    public RdfTypeComplexBase(RdfTypeFactory typeFactory) {
//        super(typeFactory);
//    }

    @Override
    public boolean isSimpleType() {
        return false;
    }
}
