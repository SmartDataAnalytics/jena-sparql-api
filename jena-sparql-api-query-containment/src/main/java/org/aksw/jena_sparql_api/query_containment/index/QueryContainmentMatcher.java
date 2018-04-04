package org.aksw.jena_sparql_api.query_containment.index;

import java.util.Map.Entry;
import java.util.stream.Stream;

public interface QueryContainmentMatcher<A, K, M> {
	A getOriginalRequest();
	A getEffectiveRequest();
	
	Stream<Entry<K, M>> match();
}
