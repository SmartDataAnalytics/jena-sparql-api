package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SeekableFromBlockSource
    implements Seekable
{
    // Index in the decoded data block
    protected int index;
    protected BlockSource blockSource;

    protected Block currentBlock;
    protected ByteBuffer currentBuffer;

    public SeekableFromBlockSource(int index, BlockSource bufferSource, Block currentBlock) {
        super();
        this.index = index;
        this.blockSource = bufferSource;
        this.currentBlock = currentBlock;
    }

    public SeekableFromBlockSource clone() {
        return new SeekableFromBlockSource(index, blockSource, currentBlock);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int n = 0;
        if(currentBlock != null) {
            ByteBuffer buf = currentBlock.newBuffer();
            buf.position(buf.position() + index);
            n = Math.min(dst.remaining(), buf.remaining());
            buf.limit(buf.position() + n);
            //dst.put(currentBlock.data, index, n);
            dst.put(buf);
            if(!nextPos(n)) {
                // If we reached the end, force the position to the end
                // TODO Avoid doing nextPos twice; add a method to force immediately
                nextPos(n - 1);
                ++index;
                n = -1;
            }
        }
        return n;
        //PageNavigator.readRemaining(dst, src)
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public long getPos() {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void setPos(long pos) {
        throw new RuntimeException("Not supported");
    }

    @Override
    public void posToStart() throws IOException {
        currentBlock = blockSource.contentAfter(-1);
        index = 0;
    }

    @Override
    public void posToEnd() throws IOException {
        currentBlock = blockSource.contentBefore(blockSource.size() + 1);
        index = (int)currentBlock.blockSize() - 1;
    }

    @Override
    public byte get() throws IOException {
        ByteBuffer buf = currentBlock.newBuffer();
        byte result = buf.get(buf.position() + index);
        //byte result = currentBuffer.get(currentBuffer.position() + index);
        return result;
    }

    @Override
    public boolean isPosBeforeStart() throws IOException {
        boolean result = index < 0;
        return result;
    }

    @Override
    public boolean isPosAfterEnd() throws IOException {
        boolean result = index >= currentBlock.blockSize();
        return result;
    }

    @Override
    public boolean nextPos(int len) throws IOException {
        int tgtIndex = index + len;
        Block tmp = currentBlock;
        while(tgtIndex >= tmp.blockSize()) {
            tgtIndex -= tmp.blockSize();
            Block next = tmp.nextBlock();
            if(next == null) {
                return false;
            }
            tmp = next;
        }
        index = tgtIndex;
        currentBlock = tmp;

        return true;
    }

    @Override
    public boolean prevPos(int len) throws IOException {
        int tgtIndex = index - len;
        Block tmp = currentBlock;
        while(tgtIndex < 0) {
            Block prev = tmp.nextBlock();
            tgtIndex += tmp.blockSize();
            if(prev == null) {
                return false;
            }
            tmp = prev;
        }
        index = tgtIndex;
        currentBlock = tmp;

        return true;
    }

    @Override
    public String readString(int len) throws IOException {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public int compareToPrefix(byte[] prefix) throws IOException {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public long binarySearch(long min, long max, byte delimiter, byte[] prefix) throws IOException {
        throw new RuntimeException("not implemented yet");
    }

}
