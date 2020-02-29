package org.aksw.jena_sparql_api.io.binseach;

public interface PageManager{
	Reference<Page> requestBufferForPage(long page);
	
	//ByteBuffer requestBufferForPage(long page);
	
	/**
	 * The pageSize. Must never change during the life time of a page manager.
	 * 
	 * @return
	 */
	int getPageSize();
	
	
	long getEndPos();
	// TODO Add a release mechanism	
}
