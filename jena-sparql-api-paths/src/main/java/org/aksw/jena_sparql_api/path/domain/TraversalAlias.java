package org.aksw.jena_sparql_api.path.domain;

import java.util.Map;

/**
 * Interface for MultiNodes
 */
public interface TraversalAlias<N> {
	/** getOrCreate the one single alias for this multi node. Raises an exception if there are already multiple aliases */
	default N one() {
		return viaAlias(null);
	}
	

	/** get or create semantics for each alias */
	N viaAlias(String alias);
	
	/**
	 * List all previously allocated traversals with their aliases
	 * 
	 * TODO Should the map be a live view?
	 * 
	 * @return
	 */
	Map<String, N> list();
}
