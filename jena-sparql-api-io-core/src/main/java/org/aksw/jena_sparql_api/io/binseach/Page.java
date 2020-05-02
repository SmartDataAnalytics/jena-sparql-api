package org.aksw.jena_sparql_api.io.binseach;

import java.nio.ByteBuffer;

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
