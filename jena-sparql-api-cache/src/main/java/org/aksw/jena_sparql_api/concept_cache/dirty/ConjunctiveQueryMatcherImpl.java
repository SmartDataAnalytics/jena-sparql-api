package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.aksw.commons.collections.reversible.ReversibleMap;
import org.aksw.commons.collections.reversible.ReversibleMapImpl;
import org.aksw.jena_sparql_api.concept_cache.domain.ConjunctiveQuery;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

public class ConjunctiveQueryMatcherImpl<K>
	implements ConjuntiveQueryMatcher<K>
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

		// TODO Auto-generated method stub
		return null;
	}


}
