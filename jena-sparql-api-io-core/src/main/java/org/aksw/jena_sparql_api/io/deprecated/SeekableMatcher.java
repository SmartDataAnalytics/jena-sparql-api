package org.aksw.jena_sparql_api.io.deprecated;

import java.io.IOException;

import org.aksw.commons.io.seekable.api.Seekable;

/**
 * A matcher over a Seekable.
 * Invoking next match changes the position of the seekable to the next match if available
 * Matchers may also match in reverse direction
 * 
 * If a match is found, the current position of the seekable is assumed to be the start of the match,
 * such that nextPos() / prevPos() move towards the end of the match
 * 
 * 
 * [&lt; reverse match direction -] [match pos] [ - forward match direction &gt;]
 * 
 * @author raven
 *
 */
public interface SeekableMatcher {
	/**
	 * Whether the matcher matches in forward direction and thus nextPos() moves towards the end of the match.
	 * If false, prevPos() moves towards the end of the match
	 * 
	 * @return
	 */
	boolean isForward();
	
	/**
	 * Reset the state of the matcher such that it can be used with a fresh seekable
	 * 
	 */
	void resetState();
	
	/**
	 * Move the position to the next match.
	 * Position is unspecified if no match was found.
	 * 
	 * TODO Add a horizon argument to limit scanning in case of infinite streams
	 * 
	 * @param seekable
	 * @return true if a match was found, false otherwise
	 */
	boolean find(Seekable seekable) throws IOException;
}
