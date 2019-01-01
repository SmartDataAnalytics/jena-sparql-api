package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;

public class RelationJoiner {
	protected Relation attrRelation;
	protected List<Var> attrJoinVars;
	
	protected Relation filterRelation;
	protected List<Var> filterJoinVars;
	
	protected boolean filterRelationFirst;
	
	public RelationJoiner(Relation attrRelation, List<Var> attrJoinVars) {
		this(attrRelation, attrJoinVars, false);
	}

	public RelationJoiner(Relation attrRelation, List<Var> attrJoinVars, boolean filterRelationFirst) {
		super();
		this.attrRelation = attrRelation;
		this.attrJoinVars = attrJoinVars;
		this.filterRelationFirst = filterRelationFirst;
	}

	
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

	
	/**
	 * Join with null is a no-op - i.e. it yields the original relation
	 * 
	 * @param c
	 * @return
	 */
	public Relation with(Relation c) {
		Relation result;
		if(c != null) {
			filterRelation = c;
			filterJoinVars = c.getVars();
		
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
	
	public Relation get() {
		List<Var> attrProjVars = attrRelation.getVars();
		
		
		Set<Var> attrVarsMentioned = attrRelation.getVarsMentioned();
		Set<Var> filterVarsMentioned = filterRelation.getVarsMentioned();
		
		Map<Var, Var> varMap = VarUtils.createJoinVarMap(attrVarsMentioned, filterVarsMentioned, attrJoinVars, filterJoinVars, null); //, varNameGenerator);

		Element attrElement = attrRelation.getElement();		
        Element filterElement = filterRelation.getElement();
        Element newFilterElement = ElementUtils.createRenamedElement(filterElement, varMap);

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
					Var effectiveFilterVar = varMap.get(rawFilterVar);
					Op attrOp = Algebra.compile(attrElement);
					Tuple<Set<Var>> tuple = OpVars.mentionedVarsByPosition(attrOp);
					canOmitJoin = tuple.get(1).contains(effectiveFilterVar);
				}
			}
        }
        
        List<Element> fes = ElementUtils.toElementList(newFilterElement);
        List<Element> aes = ElementUtils.toElementList(attrElement);
        //List<Element> combined = ElementUtils.groupIfNeeded(Iterables.concat(fes, aes));
        
        Element newElement = canOmitJoin ?
        		attrElement : filterRelationFirst
        			? ElementUtils.groupIfNeeded(Iterables.concat(fes, aes))
        			: ElementUtils.groupIfNeeded(Iterables.concat(aes, fes));
        
        Relation result = new RelationImpl(newElement, attrProjVars);
        return result;
	}
}