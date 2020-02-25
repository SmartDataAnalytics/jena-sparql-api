package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;

public class MainPlaygroundScanFile {
	public static long wcFwd(PageNavigator pageNavigator) throws IOException {
		System.out.println("Thread: " + Thread.currentThread());
		long result = 0;
		pageNavigator.posToStart();
		for(;;) {
			boolean a = pageNavigator.posToNext((byte)'\n');
			boolean b = pageNavigator.nextPos();
			boolean posChanged = a || b;
			if(!posChanged) {
				break;
			}
			++result;
		}

		return result;
	}
	
	public static long wcBwd(PageNavigator pageNavigator) throws IOException {
		System.out.println("Thread: " + Thread.currentThread());
		long result = 0;
		pageNavigator.posToEnd();
		pageNavigator.prevPos();
	
		for(;;) {
			boolean a = pageNavigator.prevPos();
			// Pos to prev can move one character before the stream if the char is not found
			boolean b = pageNavigator.posToPrev((byte)'\n');
			boolean posChanged = a || b;
			if(!posChanged) {
				break;
			}
			++result;
		}
		
		return result;
	}
	
	
	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/home/raven/Projects/Eclipse/sparql-integrate-parent/ngs/test2.nq");
//		Path path = Paths.get("/tmp/test.txt");

		try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
			PageManager pageManager = PageManagerForFileChannel.create(fileChannel);
			
			long size = fileChannel.size();			
			int n = 32;
			
			long chunkSize = size / n;
			List<PageNavigator> navs = new ArrayList<>();
			for(int i = 0; i < n; ++i) {
				long start = i * chunkSize;
				long end = i + 1 != n ? start + chunkSize : size;
				
				navs.add(new PageNavigator(pageManager, start, end));
			}

			for(int i = 0; i < 5; ++i) {
				Stopwatch sw = Stopwatch.createStarted();

				// TODO Counts are not yet exact because of lack of boundary handling
				long rawCount = navs.parallelStream()
					.mapToLong(t -> {
						try {
							return wcBwd(t);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					})
					.sum();
					
				//long count = rawCount - n + 1;
					//.collect(Collectors.toList());
				
				System.out.println("fwd: " + rawCount + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
			}
//
	
//			
//			PageNavigator pageNavigator = new PageNavigator(
//				PageManagerForFileChannel.create(fileChannel)
////				,8, 9);
//				);
////				8, 13);
//			
//			
//			pageNavigator.posToStart();
////			pageNavigator.nextPos();
//
//			if(false) {
//				byte[] arr = new byte[100];
//				int read = pageNavigator.readBytes(arr, 0, 100);
//				
//				System.out.println("Read " + read + ": " + new String(arr, 0, read));
//				System.out.println("Done");
//			}
//
//			for(int x = 0; x < 5; ++x) {
//
//				if(true) {
//					Stopwatch sw = Stopwatch.createStarted();
//					
//					long i = 0;
//					//long i = wcFwd();
//					
//					System.out.println("fwd: " + i + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001 + " " + pageNavigator.getPos());
//				}
//	
//				if(true) {
//					Stopwatch sw = Stopwatch.createStarted();
//					long i = 0;
//
//					System.out.println("bwd: " + i + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001 + " " + pageNavigator.getPos());
//				}
//
//			}
		}
	}
}
