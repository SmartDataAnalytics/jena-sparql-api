package org.aksw.jena_sparql_api.io.binsearch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.io.binseach.BufferFromInputStream;
import org.aksw.jena_sparql_api.io.binseach.ReadableByteChannelBase;
import org.junit.Test;

import com.github.jsonldjava.shaded.com.google.common.base.Stopwatch;

/**
 * A microbenchmark to evaluate BufferFromInputStream vs BufferedInputStream.
 * 
 * Initial results on my notebook
 *   java: openjdk version "1.8.0_282"
 *   cpu: Intel(R) Core(TM) i7-7700HQ CPU @ 2.80GHz
 *   ram: SODIMM DDR4 Synchronous 2400 MHz (0,4 ns)
 * 
 * Sequential read performance for 10 x 1.000.000 reads of 1024 byte blocks (ms):
 *   BufferedInputStream: 18548
 *   BufferFromInputStream: 7690
 *  
 * Sequential read performance for 1.000 x 10.000 reads of 1024 byte blocks (ms):
 *   BufferedInputStream: 9555
 *   BufferFromInputStream: 3198
 * 
 * The main difference is that the typical BufferedInputStream implementation (it may differ between jres)
 * only uses a single array as buffer that is resized by doubling the array size and copying the data
 * BufferFromInputStream doubles the size of buckets (arrays) in an array of buckets; so there is no data copying.
 * 
 * TODO evaluate re-read and random access
 * 
 * @author Claus Stadler
 * 
 */
public class BufferFromInputStreamTests {

	public static class InfiniteReadableByteChannel extends ReadableByteChannelBase {
		@Override
		protected int readActual(ByteBuffer dst) throws IOException {
			ByteBuffer tmp = dst.duplicate();

			int remaining = tmp.remaining();
			byte[] arr = new byte[remaining];
			Arrays.fill(arr, (byte) 'x');

			tmp.put(arr);

			return remaining;
		}

	}

	public static void benchSequentialRead(String label, Callable<InputStream> inSupp) throws IOException, Exception {

		Stopwatch sw = Stopwatch.createStarted();

		for (int j = 0; j < 10; ++j) {
			try (InputStream in = inSupp.call()) {
				byte[] buffer = new byte[1 * 1024];
				for (int i = 0; i < 1000000; ++i) {
					in.read(buffer);
				}
			}
		}

		System.out.println(label + ": " + sw.elapsed(TimeUnit.MILLISECONDS));
	}

	// @Test
	public void testBufferFromInputStream() throws Exception {

		InputStream in = Channels.newInputStream(new InfiniteReadableByteChannel());

		benchSequentialRead("BufferFromInputStream", () -> {
			BufferFromInputStream buf = new BufferFromInputStream(8 * 1024, in);
			return Channels.newInputStream(buf.newChannel());
		});

	}

	// @Test
	public void testBufferedInputStream() throws Exception {

		benchSequentialRead("BufferedInputStream", () -> {
			InputStream in = Channels.newInputStream(new InfiniteReadableByteChannel());
			BufferedInputStream buf = new BufferedInputStream(in);
			buf.mark(Integer.MAX_VALUE / 2);
			return buf;
		});

	}

}
