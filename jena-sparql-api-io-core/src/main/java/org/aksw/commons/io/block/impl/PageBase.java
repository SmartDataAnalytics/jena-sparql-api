package org.aksw.commons.io.block.impl;

import java.nio.ByteBuffer;

import org.aksw.commons.io.block.api.PageManager;

public class PageBase
    implements Page
{
    protected PageManager pageManager;
    protected long id;
    protected ByteBuffer originalBuffer;
    //Reference<ByteBuffer> baseBufferRef;
    //protected boolean isReleased;

    public PageBase(PageManager pageManager, long id, ByteBuffer originalBuffer) {
        super();
        this.pageManager = pageManager;
        this.id = id;
        this.originalBuffer = originalBuffer;
//		this.baseBuffer = baseBuffer;
//		this.isReleased = false;
    }

    @Override
    public long getOffset() {
        return id;
    }

    @Override
    public PageManager getPageManager() {
        return pageManager;
    }

    @Override
    public ByteBuffer newBuffer() {
        ByteBuffer result = originalBuffer.duplicate();
        return result;
    }

    @Override
    public void close() throws Exception {
    }

//	@Override
//	public void release() {
//		baseBufferRef.release();
//	}
//
//	@Override
//	public boolean isReleased() {
//		boolean result = baseBufferRef.isReleased();
//		return result;
//	}

    //public abstract void doRelease();
}
