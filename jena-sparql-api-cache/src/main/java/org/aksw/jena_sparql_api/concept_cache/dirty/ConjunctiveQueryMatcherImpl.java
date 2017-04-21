package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;
import org.aksw.jena_sparql_api.concept_cache.domain.ConjunctiveQuery;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherProjectionUtils;
import org.apache.jena.sparql.core.Var;

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

        Map<K, QfpcMatch> result = new LinkedHashMap<>();
        for(Entry<Long, QfpcMatch> e : matches.entrySet()) {
            Object patternId = e.getKey();
            QfpcMatch match = e.getValue();
            Map<Var, Var> varMap = match.getVarMap();

            Set<K> keys = keyToPatternId.reverse().get((Long)patternId);
            for(K key : keys) {
                ConjunctiveQuery candCq = keyToQuery.get(key);
                VarInfo viewVarInfo = candCq.getProjection();

                boolean isProjValid = SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, varMap, false);

                if(isProjValid) {
                    result.put(key, match);
                }

            }
        }

        return result;
    }


}
