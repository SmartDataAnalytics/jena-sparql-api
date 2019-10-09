package org.aksw.jena_sparql_api.io.endpoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.io.filter.sys.SysCallFn;
import org.aksw.jena_sparql_api.io.filter.sys.SysCallFnLbZipEncode;
import org.apache.jena.ext.com.google.common.io.ByteSource;

import com.github.jsonldjava.shaded.com.google.common.collect.ObjectArrays;
import com.google.common.base.Stopwatch;


/**
 * Class for allowing an arbitrary number of concurrent reads to a file
 * while it is being written to. Readers reaching the end of their respective channel
 * will block and wait for more data as long as the write channel remains open.
 * 
 * An endpoint combines sink and source capabilities.
 * The ConcurrentFileEndpoint is a WritableByteChannel that
 * supports creating new InputStreams over the data.
 * 
 * At present, these InputStreams are not seekable.
 *  
 * 
 * @author raven
 *
 */
public class ConcurrentFileEndpoint
	implements WritableByteChannel
{
	protected Path path;
	protected SeekableByteChannel writeChannel;
	
	protected boolean isAborted;
	
	public static ConcurrentFileEndpoint create(Path path, OpenOption ... options) throws IOException {
		SeekableByteChannel writeChannel = Files.newByteChannel(path, ObjectArrays.concat(StandardOpenOption.WRITE, options));
		return new ConcurrentFileEndpoint(path, writeChannel);
	}
	
	public ConcurrentFileEndpoint(Path path, SeekableByteChannel writeChannel) {
		this.path = path;
		this.writeChannel = writeChannel;
	}
	
	public boolean isAborted() {
		return isAborted;
	}
	
	public void abort() {
		if(isOpen()) {
			this.isAborted = true;
			try {
				close();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public boolean isOpen() {
		boolean result = writeChannel.isOpen();
		return result;
	}


	@Override
	public void close() throws IOException {
		synchronized (this) {
			writeChannel.close();
			this.notifyAll();			
		}
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		int result = writeChannel.write(src);

		synchronized (this) {
			this.notifyAll();			
		}
		return result;
	}
	
	public ReadableByteChannel newReadChannel() throws IOException {
		ReadableByteChannel result = ConcurrentFileReader.create(path, this, null);
		return result;
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		SysCallFn fn = new SysCallFnLbZipEncode();
		
		Path file = Paths.get("/tmp/myfile.nt");
		
		for(int i = 0; i < 2; ++i) {
			Stopwatch stopwatch = Stopwatch.createStarted();
			System.out.println("Lines: " + Files.lines(file).count());
			System.out.println("Time taken [scan on input file]: " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001f);
		}

		
		ConcurrentFileEndpoint endpoint = ConcurrentFileEndpoint.create(file, StandardOpenOption.CREATE);
		
		ByteSource byteSource = org.apache.jena.ext.com.google.common.io.Files.asByteSource(new File("/home/raven/Projects/Data/LSQ/deleteme.sorted.nt"));

		List<Runnable> tasks = new ArrayList<>();
		tasks.add(
				() -> {
					try(OutputStream out = Channels.newOutputStream(endpoint)) {
						byteSource.copyTo(out);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		
		int numTasks = 1;
		int numWorkers = 4;

		for(int i = 0; i < numTasks; ++i) {
			tasks.add(
				() -> {
					String line;
					BufferedReader br;
					try {
						br = new BufferedReader(new InputStreamReader(Channels.newInputStream(endpoint.newReadChannel())));
						System.out.println("Thread #" + Thread.currentThread().getId() + ": " + br.lines().count());
//						while((line = br.readLine()) != null) {
//							System.out.println("Thread #" + Thread.currentThread().getId() + ": " + line);
//						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
		
		
		Stopwatch stopwatch = Stopwatch.createStarted();
		ExecutorService es = Executors.newFixedThreadPool(Math.max(numWorkers, numTasks + 1));
		List<Future<?>> futures = tasks.stream().map(es::submit)
			.collect(Collectors.toList());
		
		es.shutdown();
		es.awaitTermination(3, TimeUnit.SECONDS);
//		es.awaitTermination(10, TimeUnit.MINUTES);
		
		for(Future<?> f : futures) {
			try {
				f.get();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Time taken [concurrent read/write]: " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) * 0.001f);
	}
}
