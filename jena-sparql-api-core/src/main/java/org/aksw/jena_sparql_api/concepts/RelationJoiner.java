package org.aksw.jena_sparql_api.concepts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
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
        boolean canOmitJoin = false;
		if(filterRelation.getVars().size() == 1) {
			UnaryRelation fr = filterRelation.toUnaryRelation();
			Var rawFilterVar = fr.getVar();
			if(fr.isSubjectConcept()) {
				Var effectiveFilterVar = varMap.get(rawFilterVar);
				Op attrOp = Algebra.compile(attrElement);
				Tuple<Set<Var>> tuple = OpVars.mentionedVarsByPosition(attrOp);
				canOmitJoin = tuple.get(1).contains(effectiveFilterVar);
			}
		}

        
        Element newElement = canOmitJoin ?
        		attrElement : filterRelationFirst
        			? ElementUtils.groupIfNeeded(newFilterElement, attrElement)
        			: ElementUtils.groupIfNeeded(attrElement, newFilterElement);
        
        Relation result = new RelationImpl(newElement, attrProjVars);
        return result;
	}
}