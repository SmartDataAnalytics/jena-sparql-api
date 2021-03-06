package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.aksw.jena_sparql_api.io.common.Reference;

/**
 * A page is a fixed size sequence of bytes obtained from a page manager backed by a ByteBuffer.
 * Only the last page may have a smaller size than the others.
 *
 * A page does not provide its own release mechanism.
 * Instead, the page manager returns a {@code Reference<Page>}.
 * A page is only released iff all its references are released, hence any release action
 * is associated with the reference. Usually the release action is performed immediately
 * by the thread that releases the last reference.
 *
 *
 *
 * @author raven
 *
 */
public interface Page
    extends Block
{
    long getOffset();

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

    default Reference<? extends Block> prevBlock() throws IOException {
        return getPageManager().contentAtOrBefore(getOffset(), false);
    }

    default Reference<? extends Page> nextBlock() throws IOException {
        return getPageManager().contentAtOrAfter(getOffset(), false);
    }


    /**
     * Check if there is a subsequent block.
     *
     * @return
     * @throws IOException
     */
    default boolean hasNext() throws IOException {
        return getPageManager().hasBlockAfter(getOffset());
    }

    default boolean hasPrev() throws IOException {
        return getPageManager().hasBlockAfter(getOffset());
    }

    default long length() throws IOException {
        return getPageManager().getSizeOfBlock(getOffset());
    }

    @Override
    default Seekable newChannel() {
        ByteBuffer buf = newBuffer();
        return new PageNavigator(new PageManagerForByteBuffer(buf));
    }
}
