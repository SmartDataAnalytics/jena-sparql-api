package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import org.aksw.jena_sparql_api.io.common.Reference;

public interface BlockSource {
    Reference<Block> contentAtOrBefore(long pos) throws IOException;
    Reference<Block> contentAtOrAfter(long pos) throws IOException;

    boolean hasBlockAfter(long pos) throws IOException;
    boolean hasBlockBefore(long pos) throws IOException;

    long getSizeOfBlock(long pos) throws IOException;

//	ByteBuffer firstContent();
//	ByteBuffer lastContent();

//	ByteBuffer getChannelForPos(long pos) throws IOException;
    long size() throws IOException;
}
