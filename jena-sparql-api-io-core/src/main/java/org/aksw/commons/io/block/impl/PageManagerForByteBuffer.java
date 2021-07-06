package org.aksw.commons.io.block.impl;

import java.nio.ByteBuffer;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;

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
    public Ref<Page> requestBufferForPage(long page) {
        Page staticPage = new PageBase(this, 0, staticBuffer);
        return RefImpl.create(staticPage, null, "Reference to static page");
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
