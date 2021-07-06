package org.aksw.jena_sparql_api.io.binseach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.io.block.impl.PageManagerForFileChannel;
import org.aksw.commons.io.block.impl.PageNavigator;
import org.aksw.commons.io.seekable.api.Seekable;

/**
 * Binary search over sorted files with memory mapped IO
 *
 * TODO Move to a separate project as this is of general use
 * TODO Allow cache to be shared between concurrent readers
 *
 * @author raven
 *
 */
public class BinarySearchOnSortedFile
    implements BinarySearcher
{
    protected Seekable baseSeekable;
    protected AutoCloseable closeAction;
    protected long channelSize;
    protected byte delimiter;

    public static BinarySearchOnSortedFile create(Path path) throws IOException {
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
        PageManager pageManager = PageManagerForFileChannel.create(channel);
        Seekable seekable = new PageNavigator(pageManager);
        long channelSize = channel.size();
        BinarySearchOnSortedFile result = new BinarySearchOnSortedFile(channel::close, seekable, channelSize, (byte)'\n');
        return result;
    }

    public BinarySearchOnSortedFile(AutoCloseable closeAction, Seekable baseSeekable, long baseSeekableSize, byte delimiter) {
        super();
        this.baseSeekable = baseSeekable;
        this.channelSize = baseSeekableSize;
        this.delimiter = delimiter;
    }

    public static InputStream newInputStream(Seekable channel, BinSearchScanState state) {
        ReadableByteChannel tmp = new ReadableByteChannelForLinesMatchingPrefix(channel, state);
        InputStream result = Channels.newInputStream(tmp);

        return result;
    }

    /**
     * The high-level search method. The search result is an input stream
     * over the matching region. The binary search will only seek the offset of the region;
     * the end detection occurs on-the-fly when serving requested data.
     *
     * @param prefix
     * @return
     */
    public InputStream search(String prefix) {
        try {
            return prefix == null ? new ByteArrayInputStream(new byte[0]) : searchCore(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream searchCore(String prefix) throws IOException {
        byte[] prefixBytes = prefix.getBytes();
        return search(prefixBytes);
    }

    @Override
    public InputStream search(byte[] prefixBytes) throws IOException {
        // -1 is the position of the delimiter before the file start
        // jump to the beginning of the file if the prefix is empty
        Seekable seeker = baseSeekable.clone();

        long matchDelimPos = prefixBytes.length == 0
            ? -1
            : seeker.binarySearch(-1, channelSize, delimiter, prefixBytes);

        InputStream result;
        if(matchDelimPos != Long.MIN_VALUE) {
            seeker.setPos(matchDelimPos);

            long posOfFirstMatch = getPosOfFirstMatch(seeker, delimiter, prefixBytes);
            seeker.setPos(posOfFirstMatch + 1);

            BinSearchScanState state = new BinSearchScanState();
            state.size = channelSize;
            state.matchDelimPos = matchDelimPos;
            state.firstDelimPos = posOfFirstMatch;
            state.prefixBytes = prefixBytes;

            result = newInputStream(seeker, state);
        } else {
            result = new ByteArrayInputStream(new byte[0]);
        }

        return result;
    }


    /**
     * The initial position is assumed point to the delimiter
     * The result will point to the initial position or a preceding delimiter
     * If there is no preceding delimiter, the seekable will be positioned before start
     *
     *
     * @param seekable
     * @param delimiter
     * @param prefix
     * @return
     * @throws IOException
     */
    public static long getPosOfFirstMatch(Seekable seekable, byte delimiter, byte[] prefix) throws IOException {
        long result = seekable.getPos();

        while(!seekable.isPosBeforeStart()) {
            // If we can't go back by 1 char, the current pos is the match
            if(!seekable.prevPos(1)) {
                break;
            }

            // If there is no prior delimiter (but we could go back)
            // We go to 1 char before the start
            if(!seekable.posToPrev(delimiter)) {
                seekable.posToStart();
            }

            // We go to the char after the delimiter or the start
            seekable.nextPos(1);

            int cmp = seekable.compareToPrefix(prefix);
            if(cmp != 0) {
                seekable.setPos(result);
                break;
            } else {
                if(!seekable.prevPos(1)) {
                    seekable.posToStart();
                }
                result = seekable.getPos();
            }
        }

        return result;
    }

    @Override
    public void close() throws Exception {
        baseSeekable.close();
        if(closeAction != null) {
            closeAction.close();
        }
    }

}
