package org.aksw.commons.io.seekable.impl;
//package org.aksw.jena_sparql_api.io.binseach;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.SeekableByteChannel;
//import java.util.function.Supplier;
//
//import org.aksw.jena_sparql_api.io.api.ChannelFactory;
//
//import com.google.common.primitives.Ints;
//
//
//public class SeekableFromChannelFactory
//    implements Seekable
//{
//    // TODO Reference to the factory - so the factory gets closed when all views on it are closed?
//    protected ChannelFactory<Seekable> channelSupplier;
//
//    public static enum PosState {
//        BEFORE_START,
//        AFTER_END,
//        VALID
//    }
//
//    protected PosState posState = PosState.VALID;
//    protected SeekableByteChannel channel;
//
//    public SeekableFromChannelFactory(ChannelFactory<SeekableByteChannel> channelSupplier) {
//        super();
//        this.channelSupplier = channelSupplier;
//        this.channel = channelSupplier.newChannel();
//    }
//
//    public synchronized SeekableFromChannelFactory clone() {
//        return new SeekableFromChannelFactory(channelSupplier);
//    }
//
//    @Override
//    public int read(ByteBuffer dst) throws IOException {
//        return channel.read(dst);
//
////        int n = 0;
////        if(currentBlock != null) {
////            ByteBuffer buf = currentBlock.newBuffer();
////            buf.position(buf.position() + index);
////            n = Math.min(dst.remaining(), buf.remaining());
////            buf.limit(buf.position() + n);
////            //dst.put(currentBlock.data, index, n);
////            dst.put(buf);
////            if(!nextPos(n)) {
////                // If we reached the end, force the position to the end
////                // TODO Avoid doing nextPos twice; add a method to force immediately
////                nextPos(n - 1);
////                ++index;
////                n = -1;
////            }
////        }
////        return n;
////        //PageNavigator.readRemaining(dst, src)
//    }
//
//    @Override
//    public boolean isOpen() {
//        return true;
//    }
//
//    @Override
//    public void close() throws IOException {
//    }
//
//    @Override
//    public long getPos() throws IOException {
//        return channel.position();
//    }
//
//    @Override
//    public void setPos(long pos) throws IOException {
//        channel.position(pos);
//    }
//
//    @Override
//    public void posToStart() {
//        posState = PosState.BEFORE_START;
//        try {
//            channel.position(0);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void posToEnd() {
//        long pos;
//        try {
//            pos = channel.size();
//            channel.position(pos);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public boolean isPosBeforeStart() throws IOException {
//        boolean result = posState.equals(PosState.BEFORE_START);
//        return result;
//    }
//
//    @Override
//    public boolean isPosAfterEnd() throws IOException {
//        // HACK Trigger preload of the data up to the current position
//        // We might want to use a dedicated ensurePreloaded() method
//        // get() should throw an exception if used outside of the valid bounds
//        get();
//
//        long pos = channel.position();
//        boolean result = pos >= channel.size();
//        return result;
//    }
//
//    @Override
//    public boolean nextPos(int len) throws IOException {
//        if(len < 0) {
//            throw new IllegalArgumentException();
//        }
//
//        if (posState == PosState.BEFORE_START) {
//            len -= 1;
//        }
//
//        boolean result = len > 0;
//        long pos = channel.position();
//        channel.position(Ints.saturatedCast(pos + len));
////
////        int tgtIndex = index + len;
////        Block tmp = currentBlock;
////        while(tgtIndex >= tmp.blockSize()) {
////            tgtIndex -= tmp.blockSize();
////            Block next = tmp.nextBlock();
////            if(next == null) {
////                return false;
////            }
////            tmp = next;
////        }
////        index = tgtIndex;
////        currentBlock = tmp;
//
//        return result;
//    }
//
//    @Override
//    public boolean prevPos(int len) throws IOException {
//        if(posState == PosState.BEFORE_START) {
//            return false;
//        }
//
//        long pos = channel.position();
//        long newPos = pos - len;
//        if(newPos >= 0) {
//            channel.position(newPos);
//        } else {
//            posState = PosState.BEFORE_START;
//        }
//
////        int tgtIndex = index - len;
////        Block tmp = currentBlock;
////        while(tgtIndex < 0) {
////            Block prev = tmp.nextBlock();
////            tgtIndex += tmp.blockSize();
////            if(prev == null) {
////                return false;
////            }
////            tmp = prev;
////        }
////        index = tgtIndex;
////        currentBlock = tmp;
//
//        return true;
//    }
//
//    @Override
//    public String readString(int len) throws IOException {
//        throw new RuntimeException("not implemented yet");
//    }
//
//    @Override
//    public long size() throws IOException {
//        return channel.size();
//    }
//
//    @Override
//    public int checkNext(int len, boolean changePos) throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//    @Override
//    public int checkPrev(int len, boolean changePos) throws IOException {
//        throw new UnsupportedOperationException();
//    }
//
//}
