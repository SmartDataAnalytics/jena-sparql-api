package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.mapper.model.RdfType;


/**
 * A path fragment exposes means that can be used to perform nested path resolutions.
 * These means have the following priorities:
 * - nextResolver
 * - rdfType
 * - javaClass: In this case, a resolver has to ask the RDF type mapping model to yield an appropriate rdfType for the java class.
 * 
 * @author raven
 *
 */
public class PathFragment
//	implements PathResolver
{
	protected Relation relation;
	protected Class<?> javaClass;
	protected RdfType rdfType;
	protected PathResolver nextResolver;
	
	public PathFragment(Relation relation, Class<?> javaClass, RdfType rdfType, PathResolver nextResolver) {
		super();
		this.relation = relation;
		this.javaClass = javaClass;
		this.rdfType = rdfType;
		this.nextResolver = nextResolver;
	}
	
	//@Override
	public RdfType getRdfType() {
		return rdfType;
	}

	//@Override
	public Relation getRelation() {
		return relation;
	}

	//@Override
	public Class<?> getJavaClass() {
		return javaClass;
	}

	//@Override
	public PathResolver getNextResolver() {
		return nextResolver;
	}
}
