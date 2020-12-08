package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

import org.aksw.jena_sparql_api.io.common.Reference;
import org.aksw.jena_sparql_api.io.common.ReferenceImpl;

/**
 * PageManager wrapper for a static buffer
 *
 * @author raven
 *
 */
public class PageManagerForByteBuffer
    implements PageManager
{
//	protected ByteBuffer staticBuffer;
    protected long pageForBuf;
    protected ByteBuffer staticBuffer;

    public PageManagerForByteBuffer(ByteBuffer staticBuffer) {
        this.staticBuffer = staticBuffer;
        //this.staticBuffer = staticBuffer;
        this.pageForBuf = 0;
    }

    @Override
    public Reference<Page> requestBufferForPage(long page) {
        Page staticPage = new PageBase(this, 0, staticBuffer);
        return ReferenceImpl.create(staticPage, null, "Reference to static page");
    }

    @Override
    public int getPageSize() {
        return staticBuffer.remaining();
    }

    @Override
    public long getEndPos() {
        return staticBuffer.remaining();
    }
}
