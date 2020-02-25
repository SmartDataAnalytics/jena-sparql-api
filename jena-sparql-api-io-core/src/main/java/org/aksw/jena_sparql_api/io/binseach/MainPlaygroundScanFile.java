package org.aksw.jena_sparql_api.io.binseach;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class MainPlaygroundScanFile {
	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/home/raven/Projects/Eclipse/sparql-integrate-parent/ngs/test2.nq");
		//Path path = Paths.get("/tmp/test.txt");

		try(FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
			PageNavigator pageNavigator = new PageNavigator(
				PageManagerForFileChannel.create(fileChannel));
			for(int x = 0; x < 5; ++x) {
				if(true) {
					Stopwatch sw = Stopwatch.createStarted();
					pageNavigator.setPos(0);
					int i = 0;
					for(;;) {
						boolean posChanged = pageNavigator.posToNext((byte)'\n');
						if(!posChanged) {
							break;
						}
						++i;
						pageNavigator.nextPos();
					}
					System.out.println(i + " " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001);
				}
	
				if(false) {
					int i = 0;
					pageNavigator.posToEnd();
					pageNavigator.prevPos();
	
					for(;;) {
						pageNavigator.posToPrev((byte)'\n');
						boolean posChanged = pageNavigator.prevPos();
						if(!posChanged) {
							break;
						}
						++i;
					}
					System.out.println(i);
				}
			}
		}
	}
}
