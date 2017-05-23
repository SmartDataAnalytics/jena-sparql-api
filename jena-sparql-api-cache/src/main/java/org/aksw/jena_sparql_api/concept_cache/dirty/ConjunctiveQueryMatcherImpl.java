package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.aksw.jena_sparql_api.algebra.utils.ConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.Sets;

public class ConjunctiveQueryMatcherImpl<K>
    implements ConjunctiveQueryMatcher<K>
{
    protected AtomicLong patternIdGeneratr = new AtomicLong();
    protected SparqlViewMatcherQfpc<Long> patternMatcher = new SparqlViewMatcherQfpcImpl<>();
    protected ReversibleMap<K, Long> keyToPatternId = new ReversibleMapImpl<>();
    protected Map<K, ConjunctiveQuery> keyToQuery = new HashMap<>();

    @Override
    public void put(K key, ConjunctiveQuery cq) {
        // TODO Re-use isomorphic pattern ID
        // TODO Maybe this should be done in a patternMatcher.alloc() function
        Long patternId = patternIdGeneratr.incrementAndGet();
        QuadFilterPatternCanonical qfpc = cq.getPattern();
        patternMatcher.put(patternId, qfpc);
        keyToPatternId.put(key, patternId);
        keyToQuery.put(key, cq);
    }

    @Override
    public void removeKey(Object key) {
        //Collection<?> patternIds = keyToPatternIds.get(key);
        Object patternId = keyToPatternId.get(key);
        patternMatcher.removeKey(patternId);
        keyToPatternId.remove(key);
        keyToQuery.remove(key);
    }


    @Override
    public Map<K, QfpcMatch> lookup(ConjunctiveQuery cq) {
        QuadFilterPatternCanonical qfpc = cq.getPattern();
        Map<Long, QfpcMatch> matches = patternMatcher.lookup(qfpc);

        VarInfo userVarInfo = cq.getProjection();
        Set<Var> requestedVars = userVarInfo.getProjectVars();

        Map<K, QfpcMatch> result = new LinkedHashMap<>();
        for(Entry<Long, QfpcMatch> e : matches.entrySet()) {
            Object patternId = e.getKey();
            QfpcMatch match = e.getValue();
            Map<Var, Var> varMap = match.getVarMap();

            // Determine the mandatory vars.
            // These are the vars of a matching view that are
            // participating in joins and expressions of the residual pattern

            Set<Var> residualVars = match.getDiffPattern().getVarsMentioned();
            Set<Var> patternVars  = match.getReplacementPattern().getVarsMentioned();
            Set<Var> mandatoryVars = Sets.intersection(patternVars, residualVars);

            // TODO Properly deal with the deduplication level!!!

            Set<K> keys = keyToPatternId.reverse().get((Long)patternId);
            for(K key : keys) {
                ConjunctiveQuery candCq = keyToQuery.get(key);
                VarInfo viewVarInfo = candCq.getProjection();

                // The view projection must cover all mandatory vars,
                Set<Var> viewProvidedVars = SetUtils.mapSet(viewVarInfo.getProjectVars(), varMap);
                boolean mandatoryVarsCovered = viewProvidedVars.containsAll(mandatoryVars);

                // and the remaining provided variables must cover all of the requested projection
                Set<Var> queryProvidedVars = Sets.union(residualVars, viewProvidedVars);
                boolean requestedVarsCovered = queryProvidedVars.containsAll(requestedVars);

                boolean isProjValid = mandatoryVarsCovered && requestedVarsCovered;
                //boolean isProjValid = SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, varMap, false);

                if(isProjValid) {
                    result.put(key, match);
                }

            }
        }

        return result;
    }


}
