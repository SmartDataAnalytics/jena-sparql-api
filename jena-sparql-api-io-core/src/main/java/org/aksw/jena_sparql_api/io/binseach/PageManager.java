package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import org.aksw.jena_sparql_api.io.common.Reference;


/**
 * A PageSource (TODO change to that naming) is a special kind of BlockSource where all blocks have the same size
 * and there is a 1:1 correspondance between a pages and non-overlapping regions in the underlying buffer.
 *
 * Consecutive page ids do not necessarily have to refer to consecutive regions.
 *
 * @author raven
 *
 */
public interface PageManager
    extends BlockSource
{
    Reference<? extends Page> requestBufferForPage(long page);

    //ByteBuffer requestBufferForPage(long page);

    /**
     * The pageSize. Must never change during the life time of a page manager.
     *
     * @return
     */
    int getPageSize();

    /**
     * The maximum position in the underlying buffer
     *
     * @return
     */
    long size();


    /**
     * Retrieve the number of available pages
     *
     */
    default long getPageCount() {
        long endPos = getPageCount();
        int pageSize = getPageSize();
        long result = endPos / pageSize;
        return result;
    }

    default Reference<? extends Page> contentAtOrBefore(long pos, boolean inclusive) throws IOException {
        Reference<? extends Page> result = inclusive
                ? requestBufferForPage(pos)
                : (hasBlockBefore(pos) ? requestBufferForPage(pos - 1) : null);
        return result;
    }

    default Reference<? extends Page> contentAtOrAfter(long pos, boolean inclusive) throws IOException {
        Reference<? extends Page> result = inclusive
                ? requestBufferForPage(pos)
                : (hasBlockAfter(pos) ? requestBufferForPage(pos + 1) : null);
        return result;
    }

    default boolean hasBlockAfter(long pos) throws IOException {
        long pageCount = getPageCount();
        boolean result = pos >= -1 && pos < pageCount;
        return result;
    }

    default boolean hasBlockBefore(long pos) throws IOException {
        long pageCount = getPageCount();
        boolean result = pos == pageCount || pos > 0;
        return result;

    }

    /**
     * Return the size of the block; all but the last block are guaranteed to have the same size
     *
     */
    default long getSizeOfBlock(long pos) throws IOException {
        int pageSize = getPageSize();
        long lastIndex = getPageCount() - 1;

        long endPos = size();

        long result = pos < lastIndex
                ? pageSize
                : (pos == lastIndex ? endPos % pageSize: 0);

        return result;
    }

}
