package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;

public class MetamodelGenerator {
	protected Function<Class<?>, EntityOps> entityOpsGenerator;
	
	public MetamodelGenerator(Function<Class<?>, EntityOps> entityOpsGenerator) {
		this.entityOpsGenerator = entityOpsGenerator;
	}
	
	public <X> EntityType<X> apply(Class<X> cls) {
		EntityOps entityOps = entityOpsGenerator.apply(cls);
		
		if(entityOps.isPrimitive()) {
			
		} else {
			for(PropertyOps pop : entityOps.getProperties()) {
				createAttribute(cls, pop);
			}
			
		}
		
		return null;
	}
	
	
	public <X> Attribute<X, ?> createAttribute(Class<X> cls, PropertyOps pop) {
		Type type = pop.getReadMethod().getGenericReturnType();
		
		if(type instanceof ParameterizedType) {
		}
		
		return null;
	}
}
