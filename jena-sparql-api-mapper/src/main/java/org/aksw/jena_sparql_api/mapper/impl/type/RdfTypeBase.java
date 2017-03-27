package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.mapper.model.RdfType;

public abstract class RdfTypeBase
    implements RdfType
{
//    protected RdfTypeFactory typeFactory;
//
//    public RdfTypeBase(RdfTypeFactory typeFactory) {
//        this.typeFactory = typeFactory;
//    }
//
//    public RdfTypeFactory getTypeFactory() {
//        return typeFactory;
//    }
	
	public PathFragment resolve(String propertyName) {
		return null;
	}
}
