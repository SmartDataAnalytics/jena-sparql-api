package org.aksw.jena_sparql_api.concept_cache.dirty;

import java.util.Map;

import org.aksw.jena_sparql_api.concept_cache.core.VarInfo;
import org.aksw.jena_sparql_api.concept_cache.domain.QuadFilterPatternCanonical;

public class SparqlViewMatcherPQfpcImpl<K>
	implements SparqlViewMatcherPQfpc<K>
{

	@Override
	public Map<K, QfpcMatch> lookup(QuadFilterPatternCanonical queryQfpc, VarInfo varInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void put(K key, QuadFilterPatternCanonical qfpc, VarInfo varInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeKey(Object key) {
		// TODO Auto-generated method stub

	}

}
