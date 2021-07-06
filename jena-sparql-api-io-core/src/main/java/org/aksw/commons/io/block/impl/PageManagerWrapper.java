package org.aksw.commons.io.block.impl;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;


/**
 * A wrapper that virtually puts a displaced page view over a delegate
 *
 * There must be a 1:1 correspondence between page and byte buffer.
 * Hence, if a virtual page stretches over multiple physical ones, the data is copied
 * into a buffer of sufficient size.
 *
 *
 * view: displacement [  ]  [  ]  [  ]  [  ]  [  ]  [  ]
 * delegate:    [  p1   ] [  p2   ] [  p3   ] [  p4   ]
 *
 * @author raven
 *
 */
public class PageManagerWrapper
    implements PageManager
{
    protected PageManager delegate;
    protected long displacement;
    protected int virtPageSize;

    public PageManagerWrapper(PageManager delegate, long displacement, int pageSize) {
        super();
        this.delegate = delegate;
        this.displacement = displacement;
        this.virtPageSize = pageSize;
    }

    @Override
    public Ref<Page> requestBufferForPage(long page) {
        int physPageSize = delegate.getPageSize();

        //page *  pageSize;
        long effPos = page * virtPageSize - displacement;
        long effPage = effPos / physPageSize;
        int effIndex = (int)effPos % physPageSize;

        long effEndPos = effPos + virtPageSize;
        long effEndPage = effEndPos / physPageSize;
        int effEndIndex = (int)effEndPos % physPageSize;


        ByteBuffer resultBuffer;
        Ref<? extends Page> delegatePage;
        if(effPage == effEndPage) {
            delegatePage = delegate.requestBufferForPage(effPage);
            ByteBuffer buf = delegatePage.get().newBuffer();
            if(buf == null) {
                resultBuffer = null;
            } else {
                int o = buf.position();
            //if(buf.remaining() > virtPageSize) {
                // We expect the page to have sufficient size
                resultBuffer = buf.duplicate();

//				int start = o + effIndex;
//				if(start < 0) {
//					// create a new buffer and pad
//
//				}

                ((Buffer)resultBuffer).position(o + effIndex);
                ((Buffer)resultBuffer).limit(o + effEndIndex);
            }
            //}
        } else {
            byte[] cpy = new byte[virtPageSize];
            resultBuffer = ByteBuffer.wrap(cpy);

            for(long i = effPage;; ++i) {
                delegatePage = delegate.requestBufferForPage(i);
                try {
                    ByteBuffer buf = delegatePage.get().newBuffer();
                    if(buf != null) {
                        int o = buf.position();

                        buf = buf.duplicate();
                        int index = i == effPage ? effIndex : 0;
                        ((Buffer)buf).position(o + index);

                        //int x = buf.remaining();
                        int take = Math.min(buf.remaining(), resultBuffer.remaining());
                        ((Buffer)buf).limit(buf.position() + take);
                        resultBuffer.put(buf);

                        if(resultBuffer.remaining() == 0) {
                            ((Buffer)resultBuffer).position(0);
                            break;
                        }
                    } else {
                        break;
                    }
                } finally {
                    try {
                        delegatePage.close();
                    } catch(Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }


        }

        // This is pretty hacky:
        // We pass a dummy reference to the buffer, but the actual release happens on the
        // local delPage attribute

        Ref<? extends Page> delPage = delegatePage;
        Page tmp = new PageBase(this, page, resultBuffer);
        Ref<Page> result = RefImpl.create(tmp, () -> {
            if(delPage != null) {
                delPage.close();
            }
        }, null);

        return result;
    }

    @Override
    public int getPageSize() {
        return virtPageSize;
    }

    @Override
    public long getEndPos() {
        return delegate.getEndPos();
    }
}
