package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DecodedDataBlock
    implements Block
{
    protected BlockSource blockSource;

    protected long blockStart;
    protected long blockEnd;

    public Block nextBlock() throws IOException {
        return blockSource.contentAfter(blockEnd);
    }

    public Block prevBlock() throws IOException {
        return blockSource.contentAtOrBefore(blockStart);
    }

    public long blockSize() {
        return data.length;
    }

    protected byte[] data;

    public DecodedDataBlock(
            BlockSource bufferSource,
            long blockStart,
            long blockEnd,
            byte[] data) {
        super();
        this.blockSource = bufferSource;
        this.blockStart = blockStart;
        this.blockEnd = blockEnd;
        this.data = data;
    }

    public BlockSource getBufferSource() {
        return blockSource;
    }

    public long getBlockStart() {
        return blockStart;
    }

    public long getBlockEnd() {
        return blockEnd;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public ByteBuffer newBuffer() {
        return ByteBuffer.wrap(data);
    }
}