package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.aksw.commons.io.seekable.api.Seekable;

import com.google.common.primitives.Ints;

public class ReadableByteChannelForLinesMatchingPrefix
    implements ReadableByteChannel {

    protected Seekable channel;
    protected BinSearchScanState state;

    protected byte delimiter = (byte)'\n';

    protected long currentDelimPos;
    protected long nextKnownDelimPos;

    public ReadableByteChannelForLinesMatchingPrefix(Seekable channel, BinSearchScanState state) {
        this.channel = channel;
        this.state = state;

        this.currentDelimPos = state.firstDelimPos;//Math.max(state.firstPos, 0);
        this.nextKnownDelimPos = currentDelimPos;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int wanted = dst.remaining();

        long currentPos = channel.getPos();
        int result = 0;

        long checkPos;
        int satisfied = 0; // number of bytes we can satisfy of the request using nextKnownDelimPos

        // Assertion: nextKnownDelimPos > currentPos

        // Advance the nextKnowDelimPos according to the demand of bytes
        try(Seekable clone = channel.clone()) {
            while((satisfied = Ints.checkedCast(nextKnownDelimPos - currentPos)) < wanted) {
                checkPos = nextKnownDelimPos(clone, delimiter, nextKnownDelimPos, state);
                if(checkPos == Long.MIN_VALUE) {
                    break;
                }
                nextKnownDelimPos = checkPos;
            }
        }

        int n = Math.min(satisfied, wanted);
        if(n <= 0) {
            // There was a bug due to a race condition when seekable.clone() was not synchronized
            // The assertion here is for this purpose
            if(n == -1) {
                throw new RuntimeException("Assertion failed - possible race condition");
            }
            if(wanted != 0) {
                result = -1;
            }
        } else {
            ByteBuffer adjustedDst = dst.duplicate();
            ((Buffer)adjustedDst).limit(adjustedDst.position() + n);

//            byte[] foobar = new byte[200];
//            ByteBuffer foo = ByteBuffer.wrap(foobar);
//            int contrib = channel.read(foo);
             int contrib = channel.read(adjustedDst);


//            if(contrib < wanted) {
//                System.out.println("here");
//            }

            result += contrib;
        }

        return result;
    }

    public static long nextKnownDelimPos(Seekable channel, byte delimiter, long currentDelimPos, BinSearchScanState state) throws IOException {

        //Seekable cp = channel.clone();

        long result;
        if(currentDelimPos < state.matchDelimPos) {
            result = state.matchDelimPos;
        } else if(currentDelimPos + 1 >= state.size) { // has exceeded end
            result = Long.MIN_VALUE;
        } else {
            int prefixLength = state.prefixBytes.length;

            boolean lineMatches;
            if(prefixLength != 0) {
                channel.setPos(currentDelimPos + 1);
//                System.out.println("byte: " + channel.get());
                lineMatches = channel.compareToPrefix(state.prefixBytes) == 0;
            } else {
                lineMatches = true;
            }

            if(lineMatches) {
//                if(channel.isPosAfterEnd() ) {
//                    result = channel.getPos();
//                } else {
                    // channel.setPos(currentDelimPos + prefixLength + state.prefixBytes.length + 1);
                channel.nextPos(prefixLength + 1);
                channel.posToNext(delimiter);
                result = channel.getPos();
//                }
            } else {
                result = Long.MIN_VALUE;
            }
        }

        return result;
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}