package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.aksw.jena_sparql_api.io.api.ChannelFactory;

public class DecodedDataBlock
    implements Block
{
    protected BlockSource blockSource;

    protected long blockStart;

    // protected long blockEnd;

    public Block nextBlock() throws Exception {
        return blockSource.contentAtOrAfter(blockStart);
    }

    public Block prevBlock() throws Exception {
        return blockSource.contentAtOrBefore(blockStart);
    }

    @Override
    public long getOffset() {
        return blockStart;
    }

//    public long blockSize() {
//        return data.length;
//    }

    // TODO: Replaces 'data'
    protected ChannelFactory<SeekableByteChannel> channelFactory;

    public DecodedDataBlock(
            BlockSource blockSource,
            long blockStart,
            ChannelFactory<SeekableByteChannel> channelFactory) {
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

    public ChannelFactory<SeekableByteChannel> getChannelFactory() {
        return channelFactory;
    }

    @Override
    public SeekableByteChannel newChannel() {
        return channelFactory.newChannel();
    }

    @Override
    public void close() throws Exception {
        channelFactory.close();
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