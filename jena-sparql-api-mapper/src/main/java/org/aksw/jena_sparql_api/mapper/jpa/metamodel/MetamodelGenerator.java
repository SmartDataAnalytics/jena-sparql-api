package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.util.function.Function;

import javax.persistence.metamodel.EntityType;

import org.aksw.jena_sparql_api.beans.model.EntityOps;

public class MetamodelGenerator {
	protected Function<Class<?>, EntityOps> entityOpsGenerator;
	
	public <X> EntityType<X> apply(Class<X> cls) {
		EntityType
		
	}
}
