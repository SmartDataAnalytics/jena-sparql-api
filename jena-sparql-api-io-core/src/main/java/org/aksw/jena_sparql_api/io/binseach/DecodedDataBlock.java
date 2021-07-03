package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.aksw.commons.io.block.api.Block;
import org.aksw.commons.io.block.api.BlockSource;
import org.aksw.commons.io.seekable.api.Seekable;
import org.aksw.commons.util.ref.Ref;
import org.aksw.jena_sparql_api.io.api.ChannelFactory;

public class DecodedDataBlock
    implements Block
{
    protected BlockSource blockSource;

    protected long blockStart;

    // protected long blockEnd;

    @Override
    public boolean hasNext() throws IOException {
        return blockSource.hasBlockAfter(blockStart);
    }

    public boolean hasPrev() throws IOException {
        return blockSource.hasBlockBefore(blockStart);
    }

    @Override
    public Ref<? extends Block> nextBlock() throws IOException {
        return blockSource.contentAtOrAfter(blockStart, false);
    }

    @Override
    public Ref<? extends Block> prevBlock() throws IOException {
        return blockSource.contentAtOrBefore(blockStart, false);
    }

    @Override
    public long getOffset() {
        return blockStart;
    }

//    public long blockSize() {
//        return data.length;
//    }

    // TODO: Replaces 'data'
    protected ChannelFactory<Seekable> channelFactory;

    public DecodedDataBlock(
            BlockSource blockSource,
            long blockStart,
            ChannelFactory<Seekable> channelFactory) {
        super();
        this.blockSource = blockSource;
        this.blockStart = blockStart;
        this.channelFactory = channelFactory;
    }

    public BlockSource getBufferSource() {
        return blockSource;
    }

    public long getBlockStart() {
        return blockStart;
    }

//    public long getBlockEnd() {
//        return blockEnd;
//    }

    public ChannelFactory<Seekable> getChannelFactory() {
        return channelFactory;
    }

    @Override
    public Seekable newChannel() {
        return channelFactory.newChannel();
    }

    @Override
    public void close() throws Exception {
        channelFactory.close();
    }

    @Override
    public long length() throws IOException {
        long result = blockSource.getSizeOfBlock(blockStart);
        return result;
    }

//    public byte[] getData() {
//        return data;
//    }
//
//    @Override
//    public ByteBuffer newBuffer() {
//        return ByteBuffer.wrap(data);
//    }
}