package org.aksw.jena_sparql_api.views.index;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concept_cache.core.ProjectedOp;
import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;
import org.aksw.jena_sparql_api.view_matcher.OpVarMap;
import org.aksw.jena_sparql_api.view_matcher.SparqlViewMatcherProjectionUtils;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Wrapper for handling projections
 *
 * @author raven
 *
 * @param <K>
 * @param <P>
 */
public class SparqlViewMatcherPopImpl<K, P>
	implements SparqlViewMatcherPop<K>
{

	SparqlViewMatcherOp<P> delegate;

    // A map to associate projections with pattern ids
    //protected Map<P, Map<K, ProjectedOp>> patternIdToKeyToPop;
    protected Multimap<P, K> patternIdToKeys;
    protected Map<K, P> keyToPatternId;

    protected Map<K, ProjectedOp> keyToPop;

    public SparqlViewMatcherPopImpl() {
    	this(
    		null,
    		HashMultimap.create(),
    		new HashMap<>(),
    		new HashMap<>()
    	);
    }

    public SparqlViewMatcherPopImpl(SparqlViewMatcherOp<P> delegate, Multimap<P, K> patternIdToKeys,
			Map<K, P> keyToPatternId, Map<K, ProjectedOp> keyToPop) {
		super();
		this.delegate = delegate;
		this.patternIdToKeys = patternIdToKeys;
		this.keyToPatternId = keyToPatternId;
		this.keyToPop = keyToPop;
	}

	/**
     * Function that filters the entries associated with a patternId by
     *
     * @param projection
     * @param patternId
     * @param varMap
     * @return
     */
    public Collection<K> lookupKeys(VarInfo userVarInfo, P patternId, Map<Var, Var> varMap) {
    	Collection<K> keys = patternIdToKeys.get(patternId);

    	Collection<K> result = keys.stream().filter(key -> {
    		VarInfo viewVarInfo = keyToPop.get(key).getProjection();
    		boolean r = SparqlViewMatcherProjectionUtils.validateProjection(viewVarInfo, userVarInfo, varMap);
    		return r;
    	}).collect(Collectors.toList());

    	return result;
    }

	@Override
	public void put(K key, ProjectedOp pop) {

		// TODO Check if the op is isomorphic to an existing pattern - in that case we could reuse a prior pattern id
		// For now we allocate a new entry

		// Remove a possible prior entry
		removeKey(key);

		Op patternOp = pop.getResidualOp();

		P patternId = delegate.allocate(patternOp);
		keyToPatternId.put(key, patternId);
		keyToPop.put(key, pop);
	}


	/**
	 * Map<K, Entry<Map<Op, Op>, Map<Var, Var>>
	 *
	 *
	 *
	 *
	 * @param pop
	 * @return
	 */
	@Override
	public Map<K, OpVarMap> lookup(ProjectedOp pop) {

		VarInfo userVarInfo = pop.getProjection();
    	Op patternOp = pop.getResidualOp();

		Map<P, OpVarMap> cands = delegate.lookup(patternOp);


		// TODO What is the result datastructure?
		//LinkedHashMap<K, OpVarMap> result = null;
		Map<K, OpVarMap> result = new HashMap<>();

		for(Entry<P, OpVarMap> cand : cands.entrySet()) {
			P patternId = cand.getKey();
			OpVarMap opVarMap = cand.getValue();

			for(Map<Var, Var> varMap : opVarMap.getVarMaps()) {
				Collection<K> keys = lookupKeys(userVarInfo, patternId, varMap);

				for(K key : keys) {
					OpVarMap e = new OpVarMap(opVarMap.getOpMap(), varMap);
					result.put(key, e);
				}
			}
		}

		return result;
	}


	@Override
	public void removeKey(Object key) {
		keyToPop.remove(key);
		P patternId = keyToPatternId.get(key);

		delegate.removeKey(patternId);
		patternIdToKeys.remove(patternId, key);
		keyToPatternId.remove(key);
	}
}