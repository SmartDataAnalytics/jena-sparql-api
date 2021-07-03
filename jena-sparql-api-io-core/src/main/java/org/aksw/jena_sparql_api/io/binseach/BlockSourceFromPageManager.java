package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.util.ref.Ref;

/**
 * A block source mainly for testing the rest of the block-based
 * machinery, such as binary search.
 *
 * TODO Can we align block source and page manager?
 *
 * Pages are backed by ByteBuffers and the assumption is, that no form of lazy loading is necessary.
 * The prime use case is memory mapped io, where ranges of file contents are made accessible via a ByteBuffer.
 * Hence, a Page only has a 'new buffer' method, which provides a mere duplicate on the buffer associated with the page.
 * Closing the page implicitly invalidates all buffers.
 * The PageNavigator provides a continuous view over a range of pages.
 *
 * In contrast, blocks need to be sequentially read. Our abstraction makes it possible to both only read
 * as much data as necessary as well as view a sequence of blocks as a continuous segment.
 * E.g. when performing binary search, only the first record of a block has to
 * be decoded in order to examine the key.
 *
 * The main difference between block and page is:
 * Pages provide access to the data using newBuffer: ByteBuffer - whereas blocks provide a
 * newChannel: Seekable method. Seekable is a subclass of Channel.
 *
 * Now the gap is: Channels have a close method, ByteBuffers don't.
 * Under this perspective, closing the channels opened from a block is not necessary,
 * as closing the block invalidates the channels and unclosed channels do not keep the block open.
 *
 *
 *
 * Blocks are created from the underlying buffer by chunking it into equal sizes
 *
 * @author raven
 *
 */
public class BlockSourceFromPageManager
    implements BlockSource
{
    protected PageManager pageManager;

    @Override
    public Ref<Block> contentAtOrBefore(long pos, boolean inclusive) throws IOException {
        // pageManager.requestBufferForPage(pos);
        return null;
    }

    @Override
    public Ref<Block> contentAtOrAfter(long pos, boolean inclusive) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasBlockAfter(long pos) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasBlockBefore(long pos) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getSizeOfBlock(long pos) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long size() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
