package org.aksw.jena_sparql_api.mapper.jpa.metamodel;

import java.awt.List;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.aksw.commons.beans.model.EntityOps;
import org.aksw.commons.beans.model.PropertyOps;

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
		Class<?> attrCls = pop.getType();
		
		String attributeName = pop.getName();
		System.out.println("Analysing attribute " + attributeName);
		
		Method m = pop.getReadMethod();
		Type type = m != null ? m.getGenericReturnType(): null;
		ParameterizedType p = type instanceof ParameterizedType ? (ParameterizedType)type : null;
		Type[] types = p != null ? p.getActualTypeArguments() : new Type[0];
		
		if(Collection.class.isAssignableFrom(attrCls)) {
						
//			if(false) {
//				// NOTE Map-like collection handling (possibly multimaps) could go here
//			} else {
			Class<?> itemClass = types != null && types.length >= 1 ? (Class<?>)types[0] : null;

			if(List.class.isAssignableFrom(attrCls)) {

			} else if(Set.class.isAssignableFrom(attrCls)) {
			
			} else { // Simply a collection
			
			}
			
			System.out.println(itemClass);
//			}
		} else if(Map.class.isAssignableFrom(attrCls)) {
			Class<?> keyClass = types != null && types.length >= 2 ? (Class<?>)types[0] : null;
			Class<?> valueClass = types != null && types.length >= 2 ? (Class<?>)types[1] : null;
			
			System.out.println(keyClass + " -> " + valueClass);
		
			//MapAttributeImpl m = new MapAttributeImpl();
			
		} else { // Singular attribute
			System.out.println(attrCls);
		}
		
		
		
		return null;
	}
}
