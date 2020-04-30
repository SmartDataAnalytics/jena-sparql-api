package org.aksw.jena_sparql_api.io.binseach;

import java.nio.channels.SeekableByteChannel;

import org.aksw.jena_sparql_api.io.api.ChannelFactory;

public interface Block
    extends ChannelFactory<SeekableByteChannel>
{
    // ByteBuffer newBuffer();


    // TODO Should we just expose the BufferFromInputStream?
    // Or should the block abstract from that?
//	long getKnownSize();
//	boolean isSizeKnon();
    long getOffset();


    Block nextBlock() throws Exception;
    Block prevBlock() throws Exception;

//	long blockSize();

//	Reference<Block> aquirePrevBlock();
//	Reference<Block> aquireNextBlock();
}
