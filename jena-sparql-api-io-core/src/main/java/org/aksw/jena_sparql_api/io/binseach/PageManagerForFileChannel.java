package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class PageManagerForFileChannel 
	implements PageManager
{
	/**
	 * The cache is crucial to the implementation.
	 * Setting its size to 0 will cause excessive allocation of pages and thus a timely
	 * OutOfMemory error.
	 * 
	 */
	protected Cache<Long, ByteBuffer> pageCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.maximumSize(64)
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
		return create(channel, 16 * 1024 * 1024);
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
	public ByteBuffer requestBufferForPage(long page) {
		ByteBuffer result;
		try {
			result = getBufferForPage(page);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@Override
	public long getEndPos() {
		return channelSize;
	}
	
	public ByteBuffer getBufferForPage(long page) throws IOException {
        long start = page * pageSize;
        long end = Math.min(channelSize, start + pageSize);
        long length = end - start;
        
        ByteBuffer result;
		try {
			result = page < 0 || length <= 0
					? null
					: pageCache.get(page, () -> {
						ByteBuffer r = channel.map(MapMode.READ_ONLY, start, length);
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

        return result;
	}
	
}
