package org.aksw.jena_sparql_api.mapper.jpa.criteria;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.PathVisitor;
import org.aksw.jena_sparql_api.mapper.jpa.criteria.expr.VPath;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

/**
 * Maps criteria path objects to mapping of variables.
 * Use every instance only for resolving a single path - then discard this visitor,
 * because of its internal state change.
 *
 * Use a supplier of such visitors for handling multiple paths.
 *
 * @author raven
 *
 */
public class PathResolverVarMapper
    implements PathVisitor<Var>
{
    protected PathResolver currentState;

    protected Set<Element> elements;
    protected Function<Expression<?>, String> aliasMapper;

    public PathResolverVarMapper(PathResolver currentState, Set<Element> elements, Function<Expression<?>, String> aliasMapper) {
        this.currentState = currentState;
        this.elements = elements;
        this.aliasMapper = aliasMapper;
    }

    public Set<Element> getElements() {
        return elements;
    }


    /**
     * Resolve a given path against a given path resolver.
     * Appends any yeld elements to 'elemenents' and returns the path's corresponding pair of
     * (source, target) variables.
     *
     */
    @Override
    public Var visit(VPath<?> path) {

        Var result;

        // If there is a parent, recurse first
        if(path instanceof Root) {
            Root<?> root = (Root<?>)path;
            //resolver = engine.createResolver(javaType);

            String rootAlias = aliasMapper.apply(root);

            // Allocate variables for the source and target node
            result = Var.alloc(rootAlias);

        } else if(path instanceof VPath) {
            Path<?> parentPath = path.getParentPath();
            VPath<?> pp = (VPath<?>)parentPath;
            Var sourceVar = pp.accept(this);

            String attrName = path.getReachingAttributeName();
            if(currentState != null) {
                currentState = currentState.resolve(attrName);

                String targetAlias = aliasMapper.apply(path);
                Var targetVar = Var.alloc(targetAlias);

                Relation r = currentState.getRelation();
                Map<Var, Var> varMap = new HashMap<>();
                varMap.put(r.getSourceVar(), sourceVar);
                varMap.put(r.getTargetVar(), targetVar);

                // Remap all remaining mentioned variables (and blank nodes)
                Set<Var> vars = r.getVarsMentioned();
                vars.remove(r.getSourceVar());
                vars.remove(r.getTargetVar());

                vars.forEach(v -> varMap.put(v, Var.alloc(targetAlias + "_" + v.getName())));


               Element renamedElement = ElementUtils.createRenamedElement(r.getElement(), varMap);
               elements.add(renamedElement);

               result = targetVar;
            } else {
                throw new RuntimeException("Could not resolve path: " + path);
            }

        } else {
            throw new RuntimeException("Don't know how to handle: " + path);
        }

        return result;
    }

//    protected Set<Var> blacklist = new HashSet<>();
//    protected Generator<Var> varGen = VarGeneratorBlacklist.create(blacklist);

    public static <T> Stream<T> enumerate(T item, Function<? super T, T> predecessorFn) {
        Stream<T> result = item == null
                ? Stream.empty()
                : Stream.concat(
                    Stream.of(item),
                    enumerate(predecessorFn.apply(item), predecessorFn));

                return result;
    }

    public static <T> T getFirstItem(T item, Function<T, T> predecessor) {
        T result = item;
        if(result != null) {
            T parentItem;
            while((parentItem = predecessor.apply(result)) != null) {
                result = parentItem;
            }
        }

        return result;
    }


    public static Path<?> getRootPath(Path<?> path) {
        @SuppressWarnings("rawtypes")
        Path<?> result = getFirstItem((Path)path, Path::getParentPath);

        return result;
    }

    public static Class<?> getRootClass(Path<?> path) {
        Path<?> rootPath = getRootPath(path);
        Class<?> result = rootPath == null ? null : rootPath.getJavaType();
        return result;
    }

    // PathResolver pathResolver,
//    public Relation resolvePath(RdfMapperEngine engine, Path<?> path) {
//        //blacklist.addAll(VarUtils.toSet(pathResolver.getAliases()));
//
//        List<Path<?>> subPaths = enumerate((Path)path, Path::getParentPath).collect(Collectors.toList());
//
//        // Sort the shorter paths (and thus the root) first
//        Collections.reverse(subPaths);
//
//        // For each path, obtain the relation
//        for(Path<?> subPath : subPaths) {
//            //Relation r =
//
//        }
//
//
//
//        Map<Expression<?>, Map<Var, Var>> exprToVarMap;
//
//
//
//        List<String> list = new ArrayList<>();
//
//        Path<?> current = path;
//        while(current != null) {
//            PathImpl<?> p = (PathImpl<?>)current;
//            list.add(p.getReachingAttributeName());
//            current = p.getParentPath();
//
//            // Do not navigate to the root, because casting it to PathImpl will fail
//            if(current.getParentPath() == null) {
//                break;
//            }
//        }
//
//        Collections.reverse(list);
//
//        PathResolver x = pathResolver;
//        for(String attr : list) {
//            if(x != null && attr != null) {
//                x = x.resolve(attr);
//            }
//        }
//
//        Relation result = x == null ? null : x.getOverallRelation(varGen);
//        System.out.println("Resolved path: " + result);
//        return result;
//    }

}
