package org.aksw.commons.io.seekable.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Supplier;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.util.closeable.AutoCloseableBase;
import org.aksw.commons.util.closeable.AutoCloseableWithLeakDetectionBase;
import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.io.binseach.BlockIterState;

import com.github.jsonldjava.shaded.com.google.common.primitives.Ints;

// Combine reference to a block with a channel
class OpenBlock{
    public Ref<? extends Block> blockRef;
    public Block block;
    public Seekable seekable;

    public OpenBlock(Ref<? extends Block> blockRef, Seekable seekable) {
        this.blockRef = blockRef;
        this.block = blockRef.get();
        this.seekable = seekable;
    }

    void close() throws IOException {
        seekable.close();
        try {
            blockRef.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

//
//    boolean hasNext() {
//
//    }
//
//    OpenedBlock closeAndNext() {
//        Reference<? extends Block> next = blockRef.get().nextBlock();
//        f
//    }
}

/**
 * Segment could have predecessor / successor methods, but
 * how can we slice segments we request?
 *
 * The use case is to scan backward until a condition is no longer satisfied,
 * maybe we don't need slicing then
 *
 *
 *
 * @author raven
 *
 */
public class SeekableFromBlock
    extends AutoCloseableWithLeakDetectionBase
    implements Seekable
{
    protected Ref<? extends Block> startBlockRef;
    protected int startPosInStartSegment;

    /**
     * The start position exposed - may be non-zero or even negative!
     */
    protected long exposedStartPos;


    /*
     *
     */

    protected long maxPos;
    protected long minPos;

    protected Ref<? extends Block> currentBlockRef;
    protected Block currentBlock; // cache of currentBlockRef.get()
    protected Seekable currentSeekable; // currentBlock.newChannel()
    protected long actualPos;


    public SeekableFromBlock(Ref<? extends Block> startBlockRef, int posInStartSegment, long exposedStartPos) {
        this(startBlockRef, posInStartSegment, exposedStartPos, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * The startBlockRef is considered to be owned by this object.
     * Closing this seekable also closes the reference.
     *
     * @param startBlockRef
     * @param posInStartSegment
     * @param exposedStartPos
     * @param minPos
     * @param maxPos
     */
    public SeekableFromBlock(Ref<? extends Block> startBlockRef, int posInStartSegment, long exposedStartPos, long minPos, long maxPos) {
        super();
        this.startBlockRef = startBlockRef;
        this.startPosInStartSegment = posInStartSegment;
        this.exposedStartPos  = exposedStartPos;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.actualPos = 0;
        init();
    }

    protected void init() {
        this.currentBlockRef = startBlockRef.acquire(null);
        this.currentBlock = currentBlockRef.get();
        this.currentSeekable = currentBlock.newChannel();
    }

    @Override
    public boolean isOpen() {
        return true;
    }


    @Override
    public void closeActual() throws Exception {
        currentSeekable.close();
        currentBlockRef.close();
        startBlockRef.close();
    }


    @Override
    public Seekable clone() {
        SeekableFromBlock result = new SeekableFromBlock(
                startBlockRef.acquire(null),
                startPosInStartSegment,
                exposedStartPos,
                minPos,
                maxPos);
        result.actualPos = this.actualPos;
        try {
            long posInSeekable = currentSeekable.getPos();
            result.currentSeekable.setPos(posInSeekable);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    @Override
    public long getPos() throws IOException {
        return actualPos;
    }


    @Override
    public void setPos(long pos) throws IOException {
        int delta = Ints.checkedCast(pos - actualPos);
        if(delta > 0) {
            checkNext(delta, true);
        } else if(delta < 0) {
            checkPrev(-delta, true);
        }

        //this.actualPos = pos;
    }


    @Override
    public void posToStart() throws IOException {
        try {
            currentBlockRef.close();
            currentBlockRef = startBlockRef.acquire(null);
            currentBlock = currentBlockRef.get();
            currentSeekable = currentBlock.newChannel();
            currentSeekable.posToStart();
            actualPos = exposedStartPos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        actualPos = -1;
    }


    // Replace end with the concept of a fwd horizon
    // We would have to read the block fully if we wanted to position at the end
    @Override
    public void posToEnd() throws IOException {
        // pos = maxPos;
        throw new UnsupportedOperationException();
//        try {
//            currentBlockRef.close();
//            currentBlockRef = startBlockRef.acquire(null);
//            currentBlock = currentBlockRef.get();
//            currentSeekable = currentBlock.newChannel();
//            currentSeekable.posToEnd();
//            actualPos = exposedStartPos;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        actualPos = maxPos;

    }


    @Override
    public boolean isPosBeforeStart() throws IOException {
        // FIXME The pos can must not be less than a min pos; the exposed start pos
        // should not be used here

        boolean result = actualPos < minPos;
        return result;
    }


    Ref<? extends Block> openNextCloseCurrent(Ref<? extends Block> current, Ref<? extends Block> exclude) throws IOException {
        Ref<? extends Block> result = current.get().nextBlock();
        try {
            if(current != exclude) {
                current.close();
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return result;
    }

    @Override
    public boolean isPosAfterEnd() throws IOException {
        // TODO Test for currentSeekable.isPosAfterEnd() and there is no non-zero size next block
        boolean result = currentSeekable.isPosAfterEnd();
        if(result) {
            BlockIterState it = BlockIterState.fwd(false, currentBlockRef, currentSeekable);
            while(it.hasNext()) {
                it.advance();
                if(!it.seekable.isPosAfterEnd()) {
                    result = true;
                    break;
                }
            }
            it.closeCurrent();
        }

        return result;
    }

    protected boolean loadNextBlock() throws IOException {
        Ref<? extends Block> nextBlockRef = currentBlockRef.get().nextBlock();
        boolean result = nextBlockRef != null;

        if(result) {
            try {
                currentBlockRef.close();
                currentSeekable.close();
            } catch (Exception e) {
                throw new IOException(e);
            }

            currentBlockRef = nextBlockRef;
            currentBlock = currentBlockRef.get();
            currentSeekable = currentBlock.newChannel();
        }

        return result;
    }

//    protected boolean loadPrevBlock() throws Exception {
//        Reference<? extends Block> nextBlockRef = currentBlockRef.get().prevBlock();
//        boolean result = nextBlockRef != null;
//
//        if(result) {
//            currentBlockRef.close();
//            currentBlockRef = nextBlockRef;
//
//            Seekable next = nextBlockRef.get().newChannel();
//            currentBlockRef.get();
//            currentBlock = currentBlockRef.get();
//
//            // Obtaining the length may already trigger a full load of the block - especially for blocks
//            // that require decoding (for bz2 this is only necessary for the last block, if there
//            // is a predecessor, a static block size can be assumed)
//            // At latest when reading this has to happen
//            long maxPos = currentBlock.length();
//            next.setPos(maxPos);
//
//            currentSeekable = next;
//        }
//
//        return result;
//    }


    class State {
        public State(Ref<? extends Block> blockRef, Seekable channel) {
            super();
            this.blockRef = blockRef;
            this.channel = channel;
        }

        Ref<? extends Block> blockRef;
        Seekable channel;
    }

    Supplier<State> nexts() {
        State current[] = new State[] { new State(currentBlockRef, currentSeekable) };
        return () -> {
            return current[0];
        };
    }


    /**
     * positive: relative position
     *
     * issue: 0 means no position change, but it does not tell whether the current position is a match or not
     * we could return a pair with
     * +0 with current position matches
     * and -0 current position does not match but no further bytes are available
     *
     */
    @Override
    public int posToNext(byte delimiter, boolean changePos) throws IOException {
        int result;
        int contrib = currentSeekable.posToNext(delimiter, changePos);

        int posDelta = 0;
        if(contrib >= 0) {
            result = contrib;
        } else {
            Ref<? extends Block> tmpBlockRef = currentBlockRef;
            Block tmpBlock = currentBlock;
            Seekable tmpSeekable = currentSeekable;

            while(contrib < 0) {
                // Add the remaining bytes of the current seekable to the posDelta
                posDelta += -contrib + 1;

                // Check whether there is a successor block
                Ref<? extends Block> nextBlockRef = contrib > 0 ? null : tmpBlockRef.get().nextBlock();

                if(nextBlockRef == null) {
                    currentBlockRef = tmpBlockRef;
                    currentBlock = tmpBlock;
                    currentSeekable = tmpSeekable;
                    tmpSeekable.posToEnd();
                } else {
                    if(tmpBlockRef != null && tmpBlockRef != currentBlockRef) {
                        tmpSeekable.close();
                        try {
                            tmpBlockRef.close();
                        } catch (Exception e) {
                        }
                    }

                    tmpBlock = nextBlockRef.get();
                    tmpSeekable = tmpBlock.newChannel();


                    contrib = tmpSeekable.posToNext(delimiter, false);


                    if(contrib > 0) {
                        currentSeekable.close();
                        try {
                            currentBlockRef.close();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        currentBlockRef = nextBlockRef;
                        currentBlock = currentBlockRef.get();
                        currentSeekable = tmpSeekable;
                        break;
                    }
                }

                try {
                    tmpBlockRef.close();
                } catch (Exception e) {
                    throw new  RuntimeException(e);
                }
                tmpSeekable.close();
            }
            result = -1;
        }

        return result;
    }


    @Override
    public int compareToPrefix(byte[] prefix) throws IOException {
        int result;
        int l = prefix.length;

        // If the current seekable has sufficient bytes available
        // delegate the call
        if(l == currentSeekable.checkNext(l, false)) {
            result = currentSeekable.compareToPrefix(prefix);
        } else {
            // Copy into array
            byte tmp[] = new byte[l];
            int available = peekNextBytes(tmp, 0, l);

            result = Seekable.compareArrays(tmp, prefix);
        }

        return result;
    }


    void setCurrent(BlockIterState state) {
        if (currentBlockRef != state.blockRef) {
            currentBlockRef.close();
            try {
                currentBlock.close();
                currentSeekable.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        currentBlockRef = state.blockRef;
        currentBlock = state.block;
        currentSeekable = state.seekable;
    }

    // void setActiveBlock(Reference<T>)

    @Override
    public int checkNext(int len, boolean changePos) throws IOException {
        // if(pos + len > maxPos)

        int result;
        int contrib = currentSeekable.checkNext(len, changePos);

        result = contrib;

        if(contrib >= len) {
            // nothing do do
        } else {
            BlockIterState it = BlockIterState.fwd(false, currentBlockRef, currentSeekable);
            while(it.hasNext()) {
                int remaining = len - result;

                it.advance();
                // Position before the start - if there are no bytes, the contrib is 0
                it.seekable.posToStart();
                contrib = it.seekable.checkNext(remaining, changePos);

                result += contrib;

                if(result >= len) {
                    break;
                }
            }

            if(changePos) {
                setCurrent(it);
            } else {
                it.closeCurrent();
            }
        }
        if(changePos) {
            actualPos += result;
        }

        return result;
    }


    @Override
    public int checkPrev(int len, boolean changePos) throws IOException {
        int result;
        int contrib = currentSeekable.checkPrev(len, changePos);

        // TODO Limit len to minPos

        result = contrib;

        if(contrib >= len) {
            // nothing do do
        } else {
            BlockIterState it = BlockIterState.bwd(false, currentBlockRef, currentSeekable);
            while(it.hasNext()) {
                int remaining = len - result;

                it.advance();

                // The following call will position 1 byte beyond the end of data
                // this may trigger a complete load of the block's content in order to determin its length
                it.seekable.posToEnd();

                contrib = it.seekable.checkPrev(remaining, changePos);
//                if(changePos && contrib > 0) {
//                    it.seekable.nextPos(1);
//                    actualPos += 1;
//                }

                result += contrib;

                if(result >= len) {
                    break;
                }
            }

            if(changePos) {
                // FIXME This does not close the current block!
                // FIXME Consolidate with loadNextBlock
                setCurrent(it);
            } else {
                it.closeCurrent();
            }
        }
        if(changePos) {
            actualPos -= result;
        }

        return result;
    }
//            int remaining = len - contrib;
//            Reference<? extends Block> tmpBlockRef = currentBlockRef;
//            Block tmpBlock = currentBlock;
//            Seekable tmpSeekable = currentSeekable;
//
//            while(remaining > 0) {
//                // Add the remaining bytes of the current seekable to the posDelta
//                remaining -= contrib;
//
//                // Check whether there is a successor block
//                Reference<? extends Block> nextBlockRef = tmpBlockRef.get().nextBlock();
//
//                if(nextBlockRef == null) {
//                    currentBlockRef = tmpBlockRef;
//                    currentBlock = tmpBlock;
//                    currentSeekable = tmpSeekable;
//                    tmpSeekable.posToEnd();
//                } else {
//                    if(tmpBlockRef != null && tmpBlockRef != currentBlockRef) {
//                        tmpSeekable.close();
//                        try {
//                            tmpBlockRef.close();
//                        } catch (Exception e) {
//                        }
//                    }
//
//                    tmpBlock = nextBlockRef.get();
//                    tmpSeekable = tmpBlock.newChannel();
//
//
//                    contrib = tmpSeekable.checkNext(remaining, false);
//
//
//                    if(contrib > 0) {
//                        currentSeekable.close();
//                        try {
//                            currentBlockRef.close();
//                        } catch (Exception e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        currentBlockRef = nextBlockRef;
//                        currentBlock = currentBlockRef.get();
//                        currentSeekable = tmpSeekable;
//                        break;
//                    }
//                }
//
//                try {
//                    tmpBlockRef.close();
//                } catch (Exception e) {
//                    throw new  RuntimeException(e);
//                }
//                tmpSeekable.close();
//            }
//            result = -1;
//        }
//
//        return result;
//    }

//    @Override
//    public boolean nextPos(int len) throws IOException {
//        int r = checkNext(len, false);
//        boolean result = r == len;
//        if(result) {
//            checkNext(len, true);
//        }
//
//        return result;
//    }

//  @Override
//  public boolean nextPos(int len) throws IOException {
//      boolean result = true;

//    @Override
//    public boolean nextPos(int len) throws IOException {
//        boolean result = true;
//
//        // Backup of the current state in case advancing by len fails
//        Seekable backup = null;
//        long backupPos = pos;
//
//        // If the segment denies relative positioning by the requested amount
//        // it implies that the end of the segment has been reached.
//        int remaining = len;
//        try {
//            for(;;) {
//                if(!currentSeekable.nextPos(remaining)) {
//                    if(backup != null) {
//                        backup = currentSeekable.clone();
//                    }
//
//                    // Check how many bytes we are away from the end
//                    // Try to extend the buffer by a certain amount and obtain how many bytes
//                    // could actually be made available
//                    int remainingBytes = Ints.checkedCast(currentSeekable.checkNext(remaining));
//
//                    if(remainingBytes > len) {
//                        throw new RuntimeException("Contract violation: relative positioning rejected despite availability of sufficient number of bytes claimed");
//                    }
//
//                    int delta = len - remainingBytes;
//                    if(!loadNextBlock()) {
//                        result = false;
//                        currentSeekable = backup;
//                        pos = backupPos;
//                    }
//
//                    remaining -= delta;
//                    continue;
//                }
//                break;
//            }
//        } finally {
//            if(result && (backup != null)) {
//                backup.close();
//            }
//        }
//
//        return result;
//    }


//    @Override
//    public boolean prevPos(int len) throws IOException {
//        boolean result = true;
//
//        // Backup of the current state in case advancing by len fails
//        Seekable backup = null;
//        long backupPos = pos;
//
//        // If the segment denies relative positioning by the requested amount
//        // it implies that the end of the segment has been reached.
//        int remaining = len;
//        try {
//            for(;;) {
//                int stepped = currentSeekable.forcePrevPos(remaining);
//                if(stepped != remaining) {
//                    if(backup != null) {
//                        backup = currentSeekable.clone();
//                    }
//
//                    int delta = len - stepped;
//                    if(!loadPrevBlock()) {
//                        result = false;
//                        currentSeekable = backup;
//                        pos = backupPos;
//                    }
//
//                    remaining -= delta;
//                    continue;
//                }
//                break;
//            }
//        } finally {
//            if(result && (backup != null)) {
//                backup.close();
//            }
//        }
//
//        return result;
//
//    }

    @Override
    public byte get() throws IOException {
        byte r = currentSeekable.get();
        return r;
    }


    @Override
    public int read(ByteBuffer dst) throws IOException {
        int contrib = 0;

        BlockIterState it = BlockIterState.fwd(true, currentBlockRef, currentSeekable);
//        boolean isFirstIteration = true;
        while(it.hasNext() && dst.remaining() > 0) {
            it.advance();

//            if(isFirstIteration) {
//                isFirstIteration = false;
//            } else {
//                it.seekable.posToStart();
//                it.seekable.nextPos(1);
//            }
            // Position before the start - if there are no bytes, the contrib is 0
            // it.seekable.posToStart();

            setCurrent(it);

            //while(it.seekable != null && dst.remaining() > 0) {
            while(dst.remaining() > 0) {
                int n = it.seekable.read(dst);
                if(n == 0) {
                    throw new RuntimeException("Read returned 0 bytes - this should never be the case");
                } else if(n == -1) {
                    break;
                }
                contrib += n;
                actualPos += n;
            }
        }

        int result = contrib == 0 && dst.remaining() > 0 ? -1 : contrib;

        return result;
    }

    public int readBroken(ByteBuffer dst) throws IOException {
        int n = -1;

        // TODO Ensure we are at the right block for the set position (probably done)
        // NOTE set position should now make sure that currentSeekable points to the right one

        // TODO Should we distinguish between the set position and the updated one?
        // probably this just adds extra complexity
        // But the SeekableByteChannel contract allows setting the position anywhere, but the read operation
        // would return -1
        // Under this perspective the extra complexity is needed...e

        int contrib = 0;
        while(currentSeekable != null && dst.remaining() > 0) {
            n = currentSeekable.read(dst);
            if(n == 0) {
                throw new RuntimeException("Read returned 0 bytes - this should never be the case");
            } else if(n == -1) {
                loadNextBlock();
                continue;
            }
            contrib += n;

            actualPos += n;
        }

        return contrib;
    }


    @Override
    public String readString(int len) throws IOException {
        throw new RuntimeException("not implemented");
    }


//    @Override
//    public boolean prevPos(int len) throws IOException {
//        int r = checkPrev(len, false);
//        boolean result = r == len;
//        if(result) {
//            checkPrev(len, true);
//        }
//
//        return result;
//    }


//    @Override
//    public int checkPrev(int len, boolean changePos) throws IOException {
//        // TODO Auto-generated method stub
//        return 0;
//    }
}
