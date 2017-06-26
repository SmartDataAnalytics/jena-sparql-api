package org.aksw.jena_sparql_api.mapper.impl.type;

import org.aksw.jena_sparql_api.concepts.Relation;


/**
 * Resolves paths of attributes to SPARQL relations, thereby allocating variable names
 *
 * @author raven
 *
 */
public interface PathResolver {

    /**
     * The property name leading to this path resolver - null for the root path
     *
     * @return
     */
    PathResolver getParent();
//	PathFragment getPathFragment();

    PathResolver resolve(String propertyName);

    Relation getRelation();
    //Relation getOverallRelation(Generator<Var> varGen);

//    PathResolver setAlias(String alias);
//    String getAlias();

    /**
     * Create a stream starting from this resolver and moving sequentially over its parents
     *
     * @return
     */
//    default Stream<PathResolver> asStream() {
//        Stream<PathResolver> result = PathResolverUtil.enumerate(this, this::getParent;);
//        return result;
//    }
//  PathResolver parent = getParent();
//
//  return Stream.concat(
//          Stream.of(this),
//          parent == null ? Stream.empty() : parent.asStream());

    /**
     * List all aliases used within the path
     * @return
     */
//    default Set<String> getAliases() {
//        Set<String> result = asStream()
//                .map(PathResolver::getAlias)
//                .filter(alias -> alias != null)
//                .collect(Collectors.toSet());
//        return result;
//    }
}
