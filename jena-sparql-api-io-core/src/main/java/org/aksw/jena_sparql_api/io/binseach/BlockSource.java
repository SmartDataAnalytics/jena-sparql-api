package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

import org.aksw.jena_sparql_api.io.common.Reference;

public interface BlockSource {
    Reference<? extends Block> contentAtOrBefore(long pos, boolean inclusive) throws IOException;
    Reference<? extends Block> contentAtOrAfter(long pos, boolean inclusive) throws IOException;

    boolean hasBlockAfter(long pos) throws IOException;
    boolean hasBlockBefore(long pos) throws IOException;

    long getSizeOfBlock(long pos) throws IOException;

//	ByteBuffer firstContent();
//	ByteBuffer lastContent();

//	ByteBuffer getChannelForPos(long pos) throws IOException;

    /**
     * Return the number of valid positions within blocks can be searched
     *
     * @return
     * @throws IOException
     */
    long size() throws IOException;
}
