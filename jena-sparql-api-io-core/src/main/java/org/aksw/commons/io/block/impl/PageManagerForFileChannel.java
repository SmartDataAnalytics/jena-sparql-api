package org.aksw.commons.io.block.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.io.block.api.PageManager;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;
import org.aksw.jena_sparql_api.io.binseach.ChannelUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Ints;

public class PageManagerForFileChannel
    implements PageManager
{
    public static final int DEFAULT_PAGE_SIZE = 16 * 1024 * 1024;

    /**
     * The cache is crucial to the implementation.
     * Setting its size to 0 will cause excessive allocation of pages and thus a timely
     * OutOfMemory error.
     *
     */
    protected Cache<Long, Ref<Page>> pageCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .maximumSize(64)
            .<Long, Ref<Page>>removalListener(notification -> {
                try {
                    notification.getValue().close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .build();

    protected FileChannel channel;

    /**
     * We rely on the channel's size being constant.
     * A growing size should work, a shrinking one will cause exceptions.
     *
     */
    protected long channelSize;

    protected int pageSize;
//	protected byte delimiter = (byte)'\n';


    public PageManagerForFileChannel(FileChannel channel, long channelSize, int pageSize) {
        super();
        this.channel = channel;
        this.channelSize = channelSize;
        this.pageSize = pageSize;
    }

    public static PageManagerForFileChannel create(FileChannel channel) throws IOException {
        return create(channel, DEFAULT_PAGE_SIZE);
    }

    public static PageManagerForFileChannel create(FileChannel channel, int pageSize) throws IOException {
        long size = channel.size();
        return new PageManagerForFileChannel(channel, size, pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public Ref<Page> requestBufferForPage(long page) {
        Ref<Page> result;
        try {
            result = getRefForPage(page);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static ByteBuffer map(FileChannel channel, MapMode mapMode, long start, long length) throws IOException {
        ByteBuffer result;

        try {
            result = channel.map(mapMode, start, length);
        } catch (UnsupportedOperationException e) {
            if (!MapMode.READ_ONLY.equals(mapMode)) {
                throw new UnsupportedOperationException(
                        "The fallback for file channels without 'map' support can only MapMode.READ_ONLY", e);
            }

            int l = Ints.saturatedCast(length);

            result = ByteBuffer.allocate(l);
            int n = ChannelUtils.readFully(channel, result, start);

            // Set the buffer's position back to the start
            result.position(0);
            // Adjust the limit
            // Note that n may be -1 if the start position of the read was beyond the end
            result.limit(Math.max(0, n));
        }

        return result;
    }

    public synchronized Ref<Page> getRefForPage(long page) throws IOException {
        long start = page * pageSize;
        long end = Math.min(channelSize, start + pageSize);
        long length = end - start;

        Ref<Page> parentRef;
        try {
            parentRef = page < 0 || length <= 0
                    ? null
                    : pageCache.get(page, () -> {
                        //ByteBuffer b = channel.map(MapMode.READ_ONLY, start, length);
                        ByteBuffer b = map(channel, MapMode.READ_ONLY, start, length);

//						System.err.println("Allocated page " + page);
                        Page p = new PageBase(this, page, b);

                        Ref<Page> r = RefImpl.create(p, () -> {
                            // System.err.println("Released primary ref to page " + page);
                        }, "Primary ref to page " + page);
                        //Page r = new PageBase(this, page, b);

// 						Copying the data to an array is slower by a factor of 3 for a 5GB file
//						if(true) {
//							byte[] cp = new byte[r.remaining()];
//							r.duplicate().get(cp);
//							r = ByteBuffer.wrap(cp);
//						}
                        return r;
                    });
        } catch (ExecutionException e) {
            throw new IOException(e);
        }

        Ref<Page> result = parentRef == null
                ? null
                : parentRef.acquire("Secondary ref to page " + page);

        return result;
    }

    @Override
    public long getEndPos() {
        return channelSize;
    }
}
