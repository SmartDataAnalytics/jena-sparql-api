package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

public interface PageManager{
	ByteBuffer requestBufferForPage(long page);
	
	/**
	 * The pageSize. Must never change during the life time of a page manager.
	 * 
	 * @return
	 */
	int getPageSize();
	
	
	long getEndPos();
	// TODO Add a release mechanism	
}
