package org.aksw.jena_sparql_api.views.index;

import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Sets;

public class SparqlViewMatcherProjectionUtils {

    /**
     *
     * allowPartial:
     * false: The view may provide at least the or even more variables than the user query
     * true: The view may provide fewer variables (but at least 1) than the user query
     *  - useful when a BGP matched partially in a larger user query BGP - in that case the view will not provide all variables
     *
     *
     *
     * @param viewVarInfo
     * @param userVarInfo
     * @param varMap
     * @param allowPartial
     * @return
     */
    public static boolean validateProjection(VarInfo viewVarInfo, VarInfo userVarInfo, Map<Var, Var> varMap, boolean allowPartial) {
        Set<Var> mappedViewVars = SetUtils.mapSet(viewVarInfo.getProjectVars(), varMap);
        mappedViewVars.remove(null);

        Set<Var> userVars = userVarInfo.getProjectVars();
        boolean result = allowPartial
                ? userVars.containsAll(mappedViewVars)
                //? !Sets.intersection(userVars, mappedViewVars).isEmpty()
                : mappedViewVars.containsAll(userVars);

//		if(result) {
//			// We passed the first check, now examine distinct vars
//			if(viewVarInfo.getDistinctLevel() > 0) {
//				// Make sure there is no overlap with non-unique vars
//				result = Sets.intersection(mappedViewVars, queryVarUsage.getNonUnique()).isEmpty();
////
////				if(result) {
////					// TODO Any more conditions? such as required distinct variables?
////				}
//
//				// Now check the distinct vars
////				Set<Var> mappedDistinctViewVars = SetUtils.mapSet(view.getDistinctVars(), varMap);
////				mappedDistinctViewVars.remove(null);
//			}
//		}

        return result;
    }



    /**
     * Returns true if the view vars described in VarInfo fit the queries var requirements expressed in VarUsage under a given varMap
     *
     * Conditions
     * (1) The mapped view variables must be a super set of the mandatory query variables
     * (2) The set of mapped distinct view variables must also be distinct in the query
     * The query's distinct vars must be
     *     (i.e: we cannot un-distinct view variables, hence the query must accept them as they are)
     *
     *
     * @param viewVarInfo
     * @param queryVarUsage
     * @param varMap
     * @return
     */
    public static boolean validateProjection(VarInfo viewVarInfo, VarUsage queryVarUsage, Map<Var, Var> varMap) {
        Set<Var> mappedViewVars = SetUtils.mapSet(viewVarInfo.getProjectVars(), varMap);
        mappedViewVars.remove(null);

        Set<Var> mandatoryVars = VarUsage.getMandatoryVars(queryVarUsage);

        boolean result = mappedViewVars.containsAll(mandatoryVars);

        if(result) {
            // We passed the first check, now examine distinct vars
            if(viewVarInfo.getDistinctLevel() > 0) {
                // Make sure there is no overlap with non-unique vars
                result = Sets.intersection(mappedViewVars, queryVarUsage.getNonUnique()).isEmpty();
//
//				if(result) {
//					// TODO Any more conditions? such as required distinct variables?
//				}

                // Now check the distinct vars
//				Set<Var> mappedDistinctViewVars = SetUtils.mapSet(view.getDistinctVars(), varMap);
//				mappedDistinctViewVars.remove(null);
            }
        }

        return result;
    }
}
