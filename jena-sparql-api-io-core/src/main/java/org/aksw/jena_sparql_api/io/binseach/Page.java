package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

public interface Page
{
	long getId();
	
	PageManager getPageManager();
	
	/**
	 * Return a byte buffer for the page with freshly
	 * initialized positioning
	 * 
	 * i.e. the returned byte buffer should be created using originalBuffer.duplicate()
	 * 
	 * @return
	 */
	ByteBuffer newBuffer();
	
	/**
	 * Release the page.
	 * No ByteBuffer obtained from this page should be used anymore
	 * 
	 */
//	void release();
//	
//	boolean isReleased();
}
