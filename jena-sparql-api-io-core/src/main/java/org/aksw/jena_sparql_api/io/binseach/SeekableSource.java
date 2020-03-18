package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public interface SeekableSource {
	/**
	 * True if Seekables obtained by the source support absolution positions
	 * 
	 * @return
	 */
	boolean supportsAbsolutePosition();
	
	Seekable get(long pos) throws IOException;
	long size() throws IOException;
}
