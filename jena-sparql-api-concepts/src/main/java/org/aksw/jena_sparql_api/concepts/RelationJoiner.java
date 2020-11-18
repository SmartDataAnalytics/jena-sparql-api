package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementOptional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class RelationJoiner {
    protected Relation attrRelation;
    protected List<Var> attrJoinVars;

    protected Relation filterRelation;
    protected List<Var> filterJoinVars;

    protected boolean filterRelationFirst;

    // Variables of the filter relation to be appended to the variables of the resulting relation
    // Note, that the variables may be renamed
    //protected List<Var> filterExtensionVars;


    /**
     * Idea to create a new projection as part of the join creation:
     * addSrcVar and addTgtVar add entries to the linked map varToOrigin
     * origin==false means, use the variable from the lhs of the join
     * origin==true means, use the variable from the rhs of the join
     *
     *
     */
    protected Map<Var, Boolean> varToOrigin = null;

    public RelationJoiner(Relation attrRelation, List<Var> attrJoinVars) {
        this(attrRelation, attrJoinVars, false);
    }

    public RelationJoiner(Relation attrRelation, List<Var> attrJoinVars, boolean filterRelationFirst) {
        super();
        this.attrRelation = attrRelation;
        this.attrJoinVars = attrJoinVars;
        this.filterRelationFirst = filterRelationFirst;
    }


//	public static RelationJoiner from(Relation r, Collection<Var> vars) {
//		RelationJoiner result = new RelationJoiner(r, vars));
//		return result;
//	}

    public static RelationJoiner from(Relation r, Var ... vars) {
        RelationJoiner result = new RelationJoiner(r, new ArrayList<>(Arrays.asList(vars)));
        return result;
    }

    public RelationJoiner addAttrJoinVar(Var var) {
        attrJoinVars.add(var);
        return this;
    }

    public RelationJoiner filterRelationFirst(boolean onOrOff) {
        this.filterRelationFirst = onOrOff;
        return this;
    }


    public RelationJoiner projectSrcVars(Var ... vars) {
        varToOrigin = varToOrigin != null ? varToOrigin : new LinkedHashMap<>();
        for(Var v : vars) {
            Boolean prior = varToOrigin.put(v, true);
            if(prior != null) {
                throw new RuntimeException("Variable " + v + " was already projected; prior value: " + prior + " - current value: " + true);
            }
         }
        return this;
    }

    public RelationJoiner projectTgtVars(Var ... vars) {
        varToOrigin = varToOrigin != null ? varToOrigin : new LinkedHashMap<>();
        for(Var v : vars) {
            Boolean prior = varToOrigin.put(v, false);
            if(prior != null) {
                throw new RuntimeException("Variable " + v + " was already projected; prior value: " + prior + " - current value: " + false);
            }
        }
        return this;
    }

    /**
     * Join with null is a no-op - i.e. it yields the original relation
     *
     * @param c
     * @param joinVars If empty, all vars of c will be used for the join
     * @return
     */
    public Relation with(Relation c, Var ... joinVars) {
        Relation result;
        if(c != null) {
            filterRelation = c;
            filterJoinVars = joinVars.length == 0 ? c.getVars() : Arrays.asList(joinVars);

            result = get();
        } else {
            result = attrRelation;
        }
        return result;
        //return this;
    }


    // This API for this method is somewhat hacky as it conflates joining with renaming; it should be revised.
    // Maybe introduce some generic operation class?
    // relation.opOn(vars).joinWith(otherRelation)
    // relation.opOn(vars).yieldRenamedFilter(filterRelation)
    public Relation yieldRenamedFilter(Relation c) {
        filterRelation = c;
        filterJoinVars = c.getVars();

        Relation result = yieldRenamedFilterCore();

        return result;
    }

    /**
     * Only yield the renamed filter portion of a 'join':
     *
     * newFilter = attrRelation.joinOn(vars).yieldRenamedFilter(filter);
     *
     *
     * @return
     */
    public Relation yieldRenamedFilterCore() {
        Set<Var> attrVarsMentioned = attrRelation.getVarsMentioned();
        Set<Var> filterVarsMentioned = filterRelation.getVarsMentioned();

        Map<Var, Var> varMap = VarUtils.createJoinVarMap(attrVarsMentioned, filterVarsMentioned, attrJoinVars, filterJoinVars, null); //, varNameGenerator);

//		Element attrElement = attrRelation.getElement();
        Element filterElement = filterRelation.getElement();
        Element newFilterElement = ElementUtils.createRenamedElement(filterElement, varMap);

        List<Var> newFilterVars = filterRelation.getVars().stream()
            .map(v -> varMap.getOrDefault(v, v))
            .collect(Collectors.toList());

        Relation result = new RelationImpl(newFilterElement, newFilterVars);
        return result;
    }

//	public List<Var> getAttrP

    /**
     * Perform variable renaming according the configuration and yield a resulting element.
     * By default, all variables of lhs are considered fixed, whereas all variables of rhs
     * are subject to renaming.
     *
     *
     * TODO This method could use some clean up.
     *
     * If we have { ?s ?p ?o }(?s) join { ?p ?s ?o }(?p)
     * we not only have to map rhs.?p->?s, but also add rhs.?s -> freshVar
     *
     * So after having set up rhs join var map,
     * for each rhs.var that maps to a var which is exists in rhs.mentionedVars, remap it to a fresh variable
     *
     * If we have { ?s ?p ?o }(?s, ?p) join { ?p ?s ?o }(?p, ?s)
     * { ?s ?p ?o } { ?s ?p ?x }
     * @return
     */
    public Relation get() {
        List<Var> attrProjVars = varToOrigin == null ? attrRelation.getVars() : new ArrayList<>(varToOrigin.keySet());

        Set<Var> attrVarsMentioned = attrRelation.getVarsMentioned();
        Set<Var> filterVarsMentioned = filterRelation.getVarsMentioned();

//		System.out.println("JOIN ON " + attrJoinVars + " --- " + filterJoinVars);
//		System.out.println(attrRelation);
//		System.out.println(filterRelation);

        // all projected attr and filters vars must NOT be renamed
        //
        // Conversely,

        // attrVars are all projected variables

        // Convention: if no projection was specified, all variables of lhs
        // are fixed (so none is undistinguished),
        // and all vars of rhs are non-distinguished
//		Set<Var> nonDistVarsLHs = Collections.emptySet();
//		Set<Var> nonDistVarsRhs = filterVarsMentioned;

        Set<Var> fixedVarsLhs = attrVarsMentioned;
        Set<Var> fixedVarsRhs = Collections.emptySet();
        if(varToOrigin != null) {
            fixedVarsLhs = varToOrigin.entrySet().stream().filter(e -> e.getValue()).map(Entry::getKey).collect(Collectors.toSet());
            fixedVarsRhs = varToOrigin.entrySet().stream().filter(e -> !e.getValue()).map(Entry::getKey).collect(Collectors.toSet());

            // non distinguished vars = those that are not projected
//			nonDistVarsLHs = Sets.difference(attrVarsMentioned, fixedVarsLhs);
//			nonDistVarsRhs = Sets.difference(filterVarsMentioned, fixedVarsRhs);
        }

        Set<Var> conflictVars = new HashSet<>(Sets.intersection(attrVarsMentioned, filterVarsMentioned));

        //Set<Var> conflictsRhs = Sets.intersection(set1, set2)

        //BiMap<Var, Var> rhsToLhs = HashBiMap.create();
        Map<Var, Var> lhsMap = new HashMap<>();
        Map<Var, Var> rhsMap = new HashMap<>();

        for (int i = 0; i < attrJoinVars.size(); ++i) {
            Var sourceJoinVar = attrJoinVars.get(i);
            Var targetJoinVar = filterJoinVars.get(i);

            rhsMap.put(targetJoinVar, sourceJoinVar);
            //lhsMap.put(key, value)
            // Map targetVar to sourceVar
            //rhsToLhs.put(targetJoinVar, sourceJoinVar);
            // rename[targetVar.getName()] = sourceVar;
        }

        Generator<Var> gen = VarGeneratorBlacklist.create(Sets.union(attrVarsMentioned, filterVarsMentioned));


        // Remap rhs
        resolveConflicts(filterVarsMentioned, fixedVarsRhs, conflictVars, lhsMap, rhsMap, gen);
        resolveConflicts(attrVarsMentioned, fixedVarsLhs, conflictVars, rhsMap, lhsMap, gen);

        // Resolve remaining conflicts; rename for lhs




        // [?a ?b] join [?x ?y] on [?b=?x] projSrc(?a) projTgt(?y)-> [?a ?y]
        // Note: It is invalid for the the same variable to be projected from lhs and rhs
        //       (even if it is used in a join (lhs.?x = rhs.?x), it should only be projected once)
        //
        // src
        //   attrFixedVars = all its projected vars
        //   varsThatMustBeRenamed = attr vars common with filter
        //

        //Map<Var, Var> varMapRhs = VarUtils.createJoinVarMap(attrVarsMentioned, nonDistVarsRhs, attrJoinVars, filterJoinVars, null); //, varNameGenerator);
        Element filterElement = filterRelation.getElement();
        Element newFilterElement = ElementUtils.createRenamedElement(filterElement, rhsMap);


        // All non-distinguished attr vars are subject to renaming
        //Map<Var, Var> attrVarMap = VarUtils.createJoinVarMap(filterVarsMentioned, nonDistVarsLHs, attrJoinVars, filterJoinVars, null); //, varNameGenerator);
        Element attrElement = attrRelation.getElement();
        Element newAttrElement = ElementUtils.createRenamedElement(attrElement, lhsMap);

//        System.out.println("-----------------------");
//        if(!newAttrElement.equals(attrElement)) {
//        	System.out.println("DEBUG POINT");
//        }
//		System.out.println(newAttrElement);
//		System.out.println(newFilterElement);


        // TODO Maybe add a flag whether omission of joins should actually be applied
        // If the filter is a subject concept and its variable appears
        // in the subject position of the attr element,
        // we can omit the filter
        boolean allowOmitJoin = true;

        boolean canOmitJoin = false;
        if(allowOmitJoin) {
            if(filterRelation.getElements().isEmpty()) {
                // TODO We may want to apply normalization - e.g. detect a group with an empty bgb
                canOmitJoin = true;
            } else if(filterRelation.getVars().size() == 1) {
                UnaryRelation fr = filterRelation.toUnaryRelation();
                Var rawFilterVar = fr.getVar();
                if(fr.isSubjectConcept()) {

                    boolean requiresJoin = false;
                    // If we are prepending an attr element that starts with
                    // OPTIONAL, we cannot omit the join

                    // TODO This rule is quite simple yet effective - we should
                    // make this more flexible though
                    if(filterRelationFirst) {
                        List<Element> elts = attrRelation.getElements();
                        if(!elts.isEmpty()) {
                            requiresJoin = elts.get(0) instanceof ElementOptional;
                        }
                    }

                    if(!requiresJoin) {
                        // We can omit with a subject concept if there is a join on the subject position
                        Var effectiveFilterVar = rhsMap.get(rawFilterVar);
                        Op attrOp = Algebra.compile(newAttrElement);
                        Tuple<Set<Var>> tuple = OpVars.mentionedVarsByPosition(attrOp);
                        canOmitJoin = tuple.get(1).contains(effectiveFilterVar);
                    }
                }
            }
        }

        List<Element> fes = ElementUtils.toElementList(newFilterElement);
        List<Element> aes = ElementUtils.toElementList(newAttrElement);
        //List<Element> combined = ElementUtils.groupIfNeeded(Iterables.concat(fes, aes));

        Element newElement = canOmitJoin ?
                newAttrElement : filterRelationFirst
                    ? ElementUtils.groupIfNeeded(Iterables.concat(fes, aes))
                    : ElementUtils.groupIfNeeded(Iterables.concat(aes, fes));

        Relation result = new RelationImpl(newElement, attrProjVars);
        return result;
    }

    public static <T> T pop(Iterable<T> items) {
        Iterator<T> it = items.iterator();
        T result = it.next();
        it.remove();
        return result;
    }

    public void resolveConflicts(Set<Var> rhsVarsMentioned, Set<Var> rhsFixedVars, Set<Var> conflictVars,
            Map<Var, Var> lhsMap, Map<Var, Var> rhsMap, Generator<Var> gen) {
        //for(Var rhsJoinVar : new HashSet<>(conflictVars)) {
        while(!conflictVars.isEmpty()) {
            Var rhsJoinVar = pop(conflictVars);

            // If the variable is fixed in rhs, its occurrence in in lhs has to be renamed
            if(!rhsFixedVars.contains(rhsJoinVar)) {

                // note: A variable can only be fixed in both lhs and rhs if it used on both sides of a join

                Set<Var> rhsJoinVars = rhsMap.keySet();
                Set<Var> rhsNonJoinVars = Sets.difference(rhsVarsMentioned, rhsJoinVars);

                // If the variable is part of the join, try the join var first
                Var targetLhsVar = rhsMap.get(rhsJoinVar);

                if(targetLhsVar != null) {
                    // Here is the case where a rhs var joins with a target var X where X occurrs
                    // as a non-joining variable in rhs

                    // If rhs.joinVar joins with another variable targetLhsVar X,
                    // where X happens to be in rhs.nonJoinVars, rename X in rhs
                    if(rhsNonJoinVars.contains(targetLhsVar)) {
                        Var rhsFreshVar = gen.next();
                        conflictVars.remove(targetLhsVar);
                        rhsMap.put(targetLhsVar, rhsFreshVar);
                    }
                } else {
                    // Here is the case where the conflict variable simply overlaps with one of lhs
                    // Here is the case where a variable X of rhs overlaps with one of lhs
                    // // an *effective* conflict variable X
                    Var rhsFreshVar = gen.next();
                    //conflictVars.remove(rhsJoinVar);
                    rhsMap.put(rhsJoinVar, rhsFreshVar);
                }

                if(false) {
                    Var targetVar = targetLhsVar == null ? rhsJoinVar : targetLhsVar;
                    // If the target var is also in conflict, allocate a fresh variable
                    // A conflict exists, if the targetVar in mentioned in rhs
                    // [(?s) x ?o ] X [?s y (?o)]
                    Var resolvedVar = !Objects.equals(rhsJoinVar, targetLhsVar) && rhsVarsMentioned.contains(targetVar)
                            ? gen.next()
                            : targetVar;



                    //rhsToLhs.put(v, resolvedVar);
                    rhsMap.put(rhsJoinVar, resolvedVar);
                }
                // Conflict for this variable resolved
                //conflictVars.remove(rhsJoinVar);

                if(false) {
//        		if(targetLhsVar != null) {
//        			conflictVars.remove(targetLhsVar);
//        			lhsMap.put(targetLhsVar, resolvedVar);
//        			// Update the join entry in the lhs map
//        		}
                }
            }
        }
    }

//
//	public static RelationJoiner join(Element a, Element b) {
//
//	}

}