package org.aksw.commons.io.block.api;

import java.io.IOException;

import org.aksw.commons.util.ref.Ref;

/**
 * A block is a sub-sequence of bytes with a fixed finite length which
 * however may be initially unknown, and thus has a certain horizon of explored length.
 * A block's full length can at any time be requested, hence a Block is a subclass of Segment.
 *
 * A block has an offset value into the sequence that contains the block.
 *
 * For example, a block
 * may backed by a bz2 decoding input stream, and only if
 * the stream hits the end, we know how much data there is
 * actually in the block.
 *
 * @author raven
 *
 */
public interface Block
    // extends ChannelFactory<SeekableByteChannel>
    extends Segment
{
    // ByteBuffer newBuffer();


    // TODO Should we just expose the BufferFromInputStream?
    // Or should the block abstract from that?
//	long getKnownSize();
//	boolean isSizeKnon();
    /**
     * Offset in a parent container; should be 0 if there is none
     *
     * @return
     */
    long getOffset();


    /**
     * The block source of this block - if any.
     *
     * @return the block source or null if there is none
     */
    // Block getBlockSource();

    Ref<? extends Block> nextBlock() throws IOException;
    Ref<? extends Block> prevBlock() throws IOException;

    /**
     * Check if there is a subsequent block.
     *
     * @return
     * @throws IOException
     */
    boolean hasNext() throws IOException;

    boolean hasPrev() throws IOException;

//	long blockSize();

//	Reference<Block> aquirePrevBlock();
//	Reference<Block> aquireNextBlock();
}
