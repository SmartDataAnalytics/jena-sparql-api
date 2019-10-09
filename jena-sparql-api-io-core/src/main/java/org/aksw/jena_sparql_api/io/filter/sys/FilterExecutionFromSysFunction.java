package org.aksw.jena_sparql_api.io.filter.sys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.endpoint.Destination;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFilter;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFile;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFileCreation;
import org.aksw.jena_sparql_api.io.endpoint.FileCreation;
import org.aksw.jena_sparql_api.io.endpoint.FileWritingProcess;
import org.aksw.jena_sparql_api.io.endpoint.FilterConfig;
import org.aksw.jena_sparql_api.io.endpoint.FilterEngine;
import org.aksw.jena_sparql_api.io.endpoint.HotFile;
import org.aksw.jena_sparql_api.io.endpoint.InputStreamSupplier;
import org.aksw.jena_sparql_api.io.utils.SimpleProcessExecutor;
import org.apache.jena.ext.com.google.common.base.StandardSystemProperty;

import com.google.common.io.ByteStreams;

import io.reactivex.Single;

public class FilterExecutionFromSysFunction 
	implements FilterConfig
{
	protected SysCallFn cmdFactory;
	protected FilterMetadata filterMetadata;
	protected Destination source;

	
	
	protected FilterExecutionFromSysFunction(SysCallFn cmdFactory, Destination source) {
		this.cmdFactory = cmdFactory;
		this.source = source;
	}

	
	/**
	 * Actually execute the stream and write the result to a file
	 * 
	 * @param path
	 * @return
	 */
//	public FileCreation execToFile(Path path) {
//		
//	}
	
	// Create a destination that creates a hot file

	@Override
	public boolean requiresFileOutput() {
		Destination effectiveInput = getEffectiveSource();
		
		Path knownInPath = extractKnownPathFromDestination(effectiveInput);
		
		// Try whether for the given input we can obtain a stream
		// If not, we asume we must create a temporary file
		boolean result = knownInPath == null
				? cmdFactory.buildCmdForStreamToStream() == null
				: cmdFactory.buildCmdForFileToStream(knownInPath) == null;
	

		// Sanity check for early detection of misbehaving cmdFactories:
		// If we cannot obtain a stream for the input, make sure that we can get the files
		
		Path dummyOutPath = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value()).resolve("sanity.check");
		boolean expectedTrue = knownInPath == null
				? cmdFactory.buildCmdForStreamToFile(dummyOutPath) != null
				: cmdFactory.buildCmdForFileToFile(knownInPath, dummyOutPath) != null;
		
		if(expectedTrue == false) {
			throw new RuntimeException("Assertion failed: Could neither obtain file nor stream output for given input");
		}
		
		return result;
	}
	
	
	public Path extractKnownPathFromDestination(Destination destination) {
		Path result
			= destination instanceof DestinationFromFile
				? ((DestinationFromFile)destination).getPath()
			: destination instanceof DestinationFromFileCreation
				? ((DestinationFromFileCreation)destination).getFileBeingCreated()
			: null;
		
		return result;
	}
	

	public Destination getEffectiveSource() {
		Destination result;
		
		/**
		 * If the source is a prior filter, obtain an effective source from it
		 * as needed.
		 * 
		 */
		if(source instanceof DestinationFilter) {
			DestinationFilter d = (DestinationFilter)source;
			FilterConfig priorFilter = d.getFilter();
			
			boolean requiresFileOutput = priorFilter.requiresFileOutput();
			
			if(requiresFileOutput) {
				// TODO Invoke callback
				//priorFilter.ifNeedsFileOutput(pathRequester, processCallback)
				Path tmpOutFile;
				try {
					tmpOutFile = Files.createTempFile("highperfstream-", ".dat");
				} catch(IOException e) {
					throw new RuntimeException(e);
				}
				//Path path = Files.newInputStream()(tmpOutFile, StandardOpenOption.CREATE);
				result = priorFilter.outputToFile(tmpOutFile); //outputToFile(tmpOutFile);
			} else {
				
				// Note: If this filter requires file input, it will stream the
				// output to a temp file down in the code
				
				result = priorFilter.outputToStream();
			}
		} else {
			result = source;
		}
		
		return result;
	}
	
	/**
	 * Execute the stream and write the result to a hot file
	 * 
	 * 
	 * TODO What if the input is a hot file? We can wait for file completion
	 * or we can connect to its stream.
	 * We always prefer the stream, unless the file is already ready, because
	 * in that case we may be able to skip the JVM
	 * 
	 * 
	 * 
	 * @param tgtPath
	 * @return
	 */
	public Single<HotFile> execToHotFile(Path tgtPath) {
		Single<HotFile> result;
		
		Destination effectiveSource = getEffectiveSource();

		
		ProcessBuilder processBuilder;
		String[] cmd;
		if((cmd = cmdFactory.buildCmdForStreamToFile(tgtPath)) != null) {
			// create a single that reads the input and forwards it to the process
			processBuilder = new ProcessBuilder(cmd);
		} else if((cmd = cmdFactory.buildCmdForStreamToStream()) != null) {
			processBuilder = new ProcessBuilder(cmd);
			// create a single that reads the input and forwards it to the process
			processBuilder.redirectOutput(tgtPath.toFile());		
		}
		
		if(processBuilder != null) {
			result = effectiveSource.prepareStream().map(inSupp -> {
				Entry<Single<Integer>, Process> e = SimpleProcessExecutor.wrap(processBuilder).executeCore();
				Single<Integer> processSingle = e.getKey();
				Process process = e.getValue();
				
				new Thread(() -> {
					try(InputStream in = inSupp.execStream()) {
						try(OutputStream out = process.getOutputStream()) {
							ByteStreams.copy(in, out);
						}
					}
				}).start();

				HotFile r = HotFileFromProcess.createStarted(tgtPath, processSingle);
				return r;
			});
		}

		// If the processor could not handle stream input, try with a concrete file

		if(result == null) {
			// Depending on the destination, we can connect obtain the info
			// whether it will create a file upon requesting a stream
			if(effectiveSource instanceof DestinationFromFileCreation) {
				DestinationFromFileCreation d = (DestinationFromFileCreation)effectiveSource;
				Single<FileCreation> fileCreation = d.getFileCreation();				
				Path inPath = d.getFileBeingCreated();

				// Probe whether any combination works with the cmdFactory
				String[] probeCmd;
				if((probeCmd = cmdFactory.buildCmdForFileToFile(inPath, tgtPath)) != null) {
				} else if((probeCmd = cmdFactory.buildCmdForFileToStream(inPath)) != null) {
				}

				if(probeCmd != null) {
					result = fileCreationToHots(fileCreation, inPath, tgtPath);
				}

			} else if(effectiveSource instanceof DestinationFromFile) {
				DestinationFromFile d = (DestinationFromFile)effectiveSource;
				Path inPath = d.getPath();
				Single<FileCreation> fileCreation = Single.just(new FileCreationWrapper(inPath));
				
				result = fileCreationToHots(fileCreation, inPath, tgtPath);
			} else {
				// We need a file but we were unable to obtain one from the destination
				// Attempt to stream the destination to a file first

				Path tmpInFile = Files.createTempFile("highperfstream-", ".dat");
				
				// Probe whether any combination works with the cmdFactory
				String[] probeCmd;
				if((probeCmd = cmdFactory.buildCmdForFileToFile(tmpInFile, tgtPath)) != null) {
				} else if((probeCmd = cmdFactory.buildCmdForFileToStream(tmpInFile)) != null) {
				} else {
					throw new RuntimeException("probing failed");
				}

				if(probeCmd != null) {
					
					effectiveSource.prepareStream().map(inSupp -> {
						try(InputStream inSupp.execStream()) {
							
						}
						
					});

					
					DestinationFromFile d = (DestinationFromFile)effectiveSource;
					Path inPath = d.getPath();
					Single<FileCreation> fileCreation = Single.just(new FileCreationWrapper(inPath));
					
					result = fileCreationToHots(fileCreation, inPath, tgtPath);


					
					result = fileCreationToHots(fileCreation, inPath, tgtPath);
				}

				
				
			}
			
		}
		
		DestinationFromFileCreation result = new DestinationFromFileCreation(tgtPath, processSingle);
		return result;
	}
	
	public Single<HotFile> fileCreationToHots(Single<? extends FileCreation> fileCreation, Path inPath, Path tgtPath) {
		
		return fileCreation.flatMap(fc -> {
			return Single.fromFuture(fc.future()).map(actualInPath -> {
				
				String[] cmd;
				
				ProcessBuilder processBuilder;
				if((cmd = cmdFactory.buildCmdForFileToFile(actualInPath, tgtPath)) != null) {
					processBuilder = new ProcessBuilder(cmd);
				} else if((cmd = cmdFactory.buildCmdForFileToStream(actualInPath)) != null) {
					processBuilder = new ProcessBuilder(cmd);
					processBuilder.redirectOutput(tgtPath.toFile());
				} else {
					throw new RuntimeException("cmdFactory could not cope with provided arguments");
				}

				Single<Integer> processSingle = SimpleProcessExecutor.wrap(processBuilder).executeFuture();
				HotFile r = HotFileFromProcess.createStarted(tgtPath, processSingle);
				return r;
			});
		});
	}


	public Single<Hotfile> trySourceAsNativeHotFile() {
		
	}
	
	
	public Single<HotFile> prepareInputAsFile() {
		
		Path file;
		
		if(source instanceof DestinationFromHotFile) {
			// Wait for the file to become ready
			DestinationFromHotFile d = (DestinationFromHotFile)source;
			Supplier<HotFile> supplier = d.getFileSupplier();
			try {
				file = supplier.get().future().get();
			} catch(InterruptedException | ExecutionException e) {
				throw new IOException(e);
			}
		}
		
		if(source instanceof DestinationFromFile) {
			DestinationFromFile d = (DestinationFromFile)source;
			file = d.getPath();
		}
		
		
		// Create a temporary file from the input
		if(file == null) {
			Single<InputStreamSupplier> streamCreation = source.execStream();
			streamCreation.map(suppIn -> {
				return (InputStreamSupplier)() -> {
					try(InputStream in = suppIn.execStream()) {
						// TODO Invoke the callback for a requested input file
						//ifNeedsFileInput(pathRequester, processCallback)
						Path tmpFile = Files.createTempFile("highperfstream", ".dat");
						
						Files.copy(in, tmpFile);
					};
				};
			});				
		}
	}
	
	
	/**
	 * Actually execute the filter
	 * 
	 * Invokes ifNeedsFileInput and ifNeedsFileOutput handlers as needed
	 * 
	 */
	@Override
	public Single<InputStream> execStream() throws IOException {
		if(filterMetadata.requiresFileInput()) {
			prepareInputAsFile();
			
			
		} else { // filter does not require file
			Single<InputStreamSupplier> in = source.execStream();
		}
		
		
	}


	@Override
	public FilterConfig ifNeedsFileInput(Supplier<Path> pathRequester,
			BiConsumer<Path, FileWritingProcess> processCallback) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public FilterConfig ifNeedsFileOutput(Supplier<Path> pathRequester,
			BiConsumer<Path, FileWritingProcess> processCallback) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public FilterConfig pipeInto(FilterEngine nextFilter) {
		FilterConfig result = nextFilter.forInput(this);
		return result;
	}


	@Override
	public DestinationFromFileCreation outputToFile(Path path) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Destination outputToStream() {
		// TODO Auto-generated method stub
		return null;
	}

}
