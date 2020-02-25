package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

/**
 * PageManager wrapper for a static buffer
 * 
 * @author raven
 *
 */
public class PageManagerForByteBuffer
	implements PageManager
{
	protected ByteBuffer buf;
	protected long pageForBuf;
	
	public PageManagerForByteBuffer(ByteBuffer buf) {
		this.buf = buf;
		this.pageForBuf = 0;
	}

	@Override
	public ByteBuffer requestBufferForPage(long page) {
		ByteBuffer result = page == pageForBuf ? buf : null;
		return result;
	}

	@Override
	public int getPageSize() {
		return buf.remaining();
	}

	@Override
	public long getEndPos() {
		return buf.remaining();
	}
}
