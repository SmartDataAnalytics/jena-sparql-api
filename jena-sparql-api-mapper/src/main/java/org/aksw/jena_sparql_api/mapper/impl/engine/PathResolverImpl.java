package org.aksw.jena_sparql_api.mapper.impl.engine;

import java.util.Map;
import java.util.function.Function;

import javax.persistence.criteria.Expression;

import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.mapper.impl.type.PathFragment;
import org.aksw.jena_sparql_api.mapper.impl.type.PathResolver;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.utils.Generator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
//
//abstract class PathResolverBase
//    implements PathResolver
//{
//    protected String alias;
//
//    @Override
//    public String getAlias() {
//        return alias;
//    }
//
//    @Override
//    public PathResolver setAlias(String alias) {
//        this.alias = alias;
//        return this;
//    }
//}


/**
 * @author raven
 *
 */
public class PathResolverImpl
    implements PathResolver
{
    protected  Map<Expression<?>, Map<Var, Var>> exprToVarMap;


    protected PathResolverImpl parent;

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

    // The property name by this path resolver was reached - null for the root
    protected String reachingPropertyName;

    //protected RdfType current;

    @Override
    public PathResolverImpl getParent() {
        return parent;
    }

    //@Override
    public PathFragment getPathFragment() {
        return pathFragment;
    }

    public PathResolverImpl(PathFragment pathFragment, RdfMapperEngine mapperEngine, String reachingPropertyName, PathResolverImpl parent) {
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


    // TODO Remove getOverallRelation; instead, compute the overall expression on each navigation step

    // Note: the varGen is assumed to be configured to avoid yielding fixed vars
//    public Relation getOverallRelation(Function<Expression<?>, String> aliasMapper) { //Generator<Var> varGen) {
//        //PathResolver parent = getParent();
//        //PathResolver grandParent = parent != null ? parent.getParent() : null;
//
//        Relation result;
//        if(pathFragment == null) {
//            result = null;
//        } else {
//            Relation contribRel = getPathFragment().getRelation();
//
//            Var srcVar;
//            Var tgtVar;
//            Element e;
//
//            String aliasName = this.getAlias();
//            aliasName = aliasName == null ? aliasMapper.apply(t);
//
//            if(aliasName != null) {
//                tgtVar = Var.alloc(aliasName);
//            } else {
//                // Conservative approach: always obtain a new var from the generator
//                tgtVar = varGen.next();
//                //tgtVar = fixedVars.contains(tgtVar) ? varGen.next() : tgtVar;
//                //tgtVar = varGen.prefer(tgtVar);
//            }
//
//
//            if(parent != null) {
//
//                Relation parentRel = parent.getOverallRelation(varGen);
//
//                Collection<Var> parentVars = parentRel.getVarsMentioned();
//
//                Collection<Var> contribInnerVars = contribRel.getInnerVars();
//
//                // - Make any intermediary var of the contribRel distinct from union(fixedVars, vars(parentRel))
//                // - If there is an alias, map contribRel.tgtVar -> alias; otherwise allocate a fresh name
//                Map<Var, Var> varMap = VarUtils.createDistinctVarMap(parentVars, contribInnerVars, true, varGen);
//
//                varMap.put(contribRel.getSourceVar(), parentRel.getSourceVar());
//                varMap.put(contribRel.getTargetVar(), tgtVar);
//
//                contribRel = contribRel.applyNodeTransform(new NodeTransformRenameMap(varMap));
//
//                srcVar = parentRel.getSourceVar();
//                e = ElementUtils.groupIfNeeded(parentRel.getElement(), contribRel.getElement());
//            } else {
//                srcVar = contribRel.getSourceVar(); //varGen.next();
//                e = contribRel.getElement();
//            }
//
//            result = new Relation(e, srcVar, tgtVar);
//            // if there is an alias, replace the target with it
//        }
//
//        return result;
//    }

    @Override
    public Relation getRelation() {
        Relation result = pathFragment.getRelation();
        return result;
    }

//	@Override
//	public Relation getOverallRelation(Generator<Var> varGen) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Relation getOverallRelation() {
//		Set<Var> fixedVars = VarUtils.toSet(getAliases());
//		Generator<Var> varGen = VarGeneratorBlacklist.create(fixedVars);
//
//		Relation result = getOverallRelation(varGen);
//
//		return result;
//	}

}
