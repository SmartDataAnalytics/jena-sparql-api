package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.jena_sparql_api.algebra.analysis.VarInfo;
import org.apache.jena.sparql.core.Var;

/**
 * Map that associates key and value objects via a varInfo object.
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class ProjectionMap<K, V> {
    protected ReversibleMap<K, V> keyToPatternId;
    protected Map<K, VarInfo> keyToVarInfo;

    public void put(K key, V value, VarInfo varInfo) {
        keyToPatternId.put(key,  value);
        keyToVarInfo.put(key, varInfo);
    }

    public void remove(Object key) {
        keyToPatternId.remove(key);
        keyToVarInfo.remove(key);
    }

    public void removeValue(Object value) {
        Set<K> keys = keyToPatternId.reverse().removeAll(value);
        keys.forEach(keyToVarInfo::remove);

    }

    public Collection<K> lookup(V patternId, VarInfo userVarInfo, Map<Var, Var> varMap) {
        Collection<K> keys = keyToPatternId.reverse().get(patternId);

        Collection<K> result = keys.stream().filter(key -> {
            VarInfo viewVarInfo = keyToVarInfo.get(key);
            boolean r = SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, varMap, false);
            return r;
        }).collect(Collectors.toList());

        return result;
    }
}