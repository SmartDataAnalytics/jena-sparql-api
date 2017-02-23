package org.aksw.jena_sparql_api.mapper.impl.type;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.sparql.core.Var;

public interface PathResolver {
	
	/**
	 * The property name leading to this path resolver - null for the root path
	 * 
	 * @return
	 */
	PathResolver getParent();
//	PathFragment getPathFragment();

	PathResolver resolve(String propertyName);

	Relation getOverallRelation(Generator<Var> varGen);
	
	PathResolver setAlias(String alias);
	String getAlias();
	
	/**
	 * Create a stream starting from this resolver and moving sequentially over its parents
	 * 
	 * @return
	 */
	default Stream<PathResolver> asStream() {
		PathResolver parent = getParent();

		return Stream.concat(
                Stream.of(this),
                parent == null ? Stream.empty() : parent.asStream());
	}
	
	/**
	 * List all aliases used within the path
	 * @return
	 */
	default Set<String> getAliases() {
		Set<String> result = asStream()
				.map(PathResolver::getAlias)
				.filter(alias -> alias != null)
				.collect(Collectors.toSet());
		return result;
	}
}
