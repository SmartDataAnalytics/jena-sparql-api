package org.aksw.jena_sparql_api.conjure.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * Attempts to calculate the size of a file or directory.
	 * 
	 * <p>
	 * Since the operation is non-atomic, the returned value may be inaccurate.
	 * However, this method is quick and does its best.
	 * 
	 * Source: https://stackoverflow.com/questions/2149785/get-size-of-folder-or-file/19877372#19877372
	 */
	public static long size(Path path) {

	    final AtomicLong size = new AtomicLong(0);

	    try {
	        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
	            @Override
	            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

	                size.addAndGet(attrs.size());
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult visitFileFailed(Path file, IOException exc) {

	                System.out.println("skipped: " + file + " (" + exc + ")");
	                // Skip folders that can't be traversed
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

	                if (exc != null)
	                    System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
	                // Ignore errors traversing a folder
	                return FileVisitResult.CONTINUE;
	            }
	        });
	    } catch (IOException e) {
	        throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
	    }

	    return size.get();
	}
	
	
	public static Path allocateTmpFile(Path tgt) {
		Path result;
		for(int i = 0; ; ++i) {
			String idx = i == 0 ? "" : "-" + i;
			result = tgt.getParent().resolve(tgt.getFileName().toString() + idx + ".tmp");
			
			// Check if the file is stale - no changes within a certain amount of time
			// TODO Improve the stale check to iterate all available files according to the pattern
			// e.g. if x.tmp does not exist, a file x-3.tmp will never get deleted with the current approach 
			try {
				if(Files.exists(result)) {
					FileTime ft = Files.getLastModifiedTime(result);
					Instant now = Instant.now();
					Instant then = ft.toInstant();
					
					long numHours = ChronoUnit.HOURS.between(then, now);
					if(numHours > 24) {
						logger.info("Removing apparently stale tmp file " + result);
						Files.delete(result);
					}
				}
			} catch(Exception e) {
				logger.warn("Attempt to check stale state and potentially removal of file failed " + result, e);
			}
			
			if(!Files.exists(result)) {
				try {
					// There is is tiny chance that creation fails
					// because another process created the file just now
					// The more likely case for the call below to fail is some write error due to
					// out of disk space, privileges or file system errors
					Files.createFile(result);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
				break;
			}
		}
		
		return result;
	}
	
	
}