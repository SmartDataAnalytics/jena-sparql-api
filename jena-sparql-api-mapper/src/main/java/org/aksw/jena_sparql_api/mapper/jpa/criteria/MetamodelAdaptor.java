package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import javax.persistence.metamodel.EntityType;

import org.aksw.jena_sparql_api.mapper.impl.type.RdfClass;
import org.aksw.jena_sparql_api.mapper.jpa.metamodel.MetamodelImpl;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;

public class MetamodelAdaptor
	extends MetamodelImpl
{
	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		RdfTypeFactory rdfTypeFactory = null;
		RdfType rdfType = rdfTypeFactory.forJavaType(cls);
		RdfClass x;
		EntityType y;
		y.getAttribute(name)
		//x.getPropertyMappers()
	}
}
