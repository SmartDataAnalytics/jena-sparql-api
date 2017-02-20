package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.function.Function;

import org.aksw.jena_sparql_api.mapper.impl.type.PathFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.model.RdfType;

/**
 * @author raven
 *
 */
public class PathResolverImpl
	implements PathResolver
{	
	protected PathResolver parent;
	
	/**
	 * The current pathFragment - null for the root path resolver
	 */
	protected PathFragment pathFragment;

	/**
	 * TODO: Instead of referring to the engine, it would be more modular if
	 * we referred to some RdfMappingMetamodel / RdfMappingModule object.
	 * Similar to JPA's metamodel.
	 * 
	 */
	protected RdfMapperEngine mapperEngine;
	//protected Function<String, PathResolver> 
	
	protected String reachingPropertyName;
	
	//protected RdfType current;

	@Override
	public PathResolver getParent() {
		return parent;
	}
	
	@Override
	public PathFragment getPathFragment() {
		return pathFragment;
	}
	
	public PathResolverImpl(PathFragment pathFragment, RdfMapperEngine mapperEngine, String reachingPropertyName, PathResolver parent) {
		super();
		this.pathFragment = pathFragment;
		this.mapperEngine = mapperEngine;
		this.reachingPropertyName = reachingPropertyName;
		this.parent = parent;
	}

	public PathResolver resolve(Function<Class<?>, RdfType> rdfTypeFactory, Class<?> javaClass, String propertyName) {
		RdfType rdfType = rdfTypeFactory.apply(javaClass);//mapperEngine.getRdfTypeFactory().forJavaType(javaClass);
		
		PathResolver result = resolve(rdfType, propertyName);
		return result;
	}
	
	public PathResolver resolve(RdfType rdfType, String propertyName) {
		PathFragment pathFragment = rdfType.resolve(propertyName);
		PathResolver result = new PathResolverImpl(pathFragment, mapperEngine, propertyName, this);
		return result;
	}
	
	public PathResolver resolve(RdfMapperEngine mapperEngine, PathFragment pathFragment, String propertyName) {
		PathResolver result;

		PathResolver tmp = pathFragment.getNextResolver();
				
		if(tmp != null) {
			result = tmp.resolve(propertyName);						
		} else {
			RdfType rdfType = pathFragment.getRdfType();
			if(rdfType != null) {
				result = resolve(rdfType, propertyName);
			} else {
				Class<?> javaClass = pathFragment.getJavaClass();
				if(javaClass != null) {
					result = resolve(mapperEngine.getRdfTypeFactory()::forJavaType, javaClass, propertyName);
				} else {
					throw new RuntimeException("Could not resolve pathFragment: " + pathFragment);
				}
			}
		}

		return result;
	}

	@Override
	public PathResolver resolve(String propertyName) {
		PathResolver result = resolve(mapperEngine, pathFragment, propertyName);
		return result;
	}
}
