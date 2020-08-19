package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;

public abstract class ReadableByteChannelBase
    implements ReadableByteChannel
{
    protected int maxReadSize;
    protected boolean isOpen = true;

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if(!isOpen) {
            throw new ClosedChannelException();
        } else if (isUnderlyingEntityKnownToBeClosed()) {
            throw new ClosedChannelException(); //"Channel was not closed, but the underlying entity was");
        }

        int result = readActual(dst);
        return result;
    }

    protected boolean isUnderlyingEntityKnownToBeClosed() {
        return false;
    }

    protected abstract int readActual(ByteBuffer dst) throws IOException;
}