package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;

public class SeekableSourceFromBufferSource
    implements SeekableSource
{
    protected BlockSource bufferSource;

    public SeekableSourceFromBufferSource(BlockSource bufferSource) {
        super();
        this.bufferSource = bufferSource;
    }

    @Override
    public boolean supportsAbsolutePosition() {
        return true;
    }

    @Override
    public Seekable get(long pos) throws IOException {
        Block block = bufferSource.contentAtOrBefore(pos);
        Seekable result = null;
        if(block != null) {
            result = new SeekableFromBlockSource(block);
        }

        return result;
    }

    @Override
    public long size() throws IOException {
        long result = bufferSource.size();
        return result;
    }


}
