package org.aksw.jena_sparql_api.sparql_path.api;

import io.reactivex.Flowable;

/**
 * A {@link PathSearch} represents a search of paths under given parameters.
 * This interface provides methods to limit the number of results and their lengths.
 * Implementations may e.g. forward these parameters to the backing core algorithm -
 * or they may simply post-process a stream of paths by these conditions.
 * 
 * @author Claus Stadler, Nov 11, 2018
 *
 * @param <P>
 */
public interface PathSearch<P> {
	
	default PathSearch<P> setMaxPathLength(Integer maxLength) {
		return setMaxLength(maxLength == null ? null : maxLength.longValue());
	}

	PathSearch<P> setMaxLength(Long maxLength);
	Long getMaxLength();
	
	default PathSearch<P> setMaxInternalResultsHint(Integer maxResults) {
		return setMaxInternalResultsHint(maxResults == null ? null : maxResults.longValue());
	}

	/**
	 * Internal 
	 * 
	 * @param maxResults
	 * @return
	 */
	PathSearch<P> setMaxInternalResultsHint(Long maxResults);
	Long getMaxInternalResultsHint();

	
//	PathSearch<P> setRandom(Random random);
	//PathSearch<P> setMaxLength(Long maxLength);

	
	Flowable<P> exec();
}
