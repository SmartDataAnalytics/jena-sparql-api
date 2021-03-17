package org.aksw.jena_sparql_api.io.filter.sys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.aksw.commons.io.endpoint.FileCreation;
import org.aksw.commons.io.process.util.SimpleProcessExecutor;
import org.aksw.jena_sparql_api.io.endpoint.ConcurrentFileEndpoint;
import org.aksw.jena_sparql_api.io.endpoint.Destination;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFilter;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFile;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFileCreation;
import org.aksw.jena_sparql_api.io.endpoint.FileWritingProcess;
import org.aksw.jena_sparql_api.io.endpoint.FilterConfig;
import org.aksw.jena_sparql_api.io.endpoint.FilterEngine;
import org.aksw.jena_sparql_api.io.endpoint.HotFile;
import org.aksw.jena_sparql_api.io.endpoint.HotFileFromJava;
import org.aksw.jena_sparql_api.io.endpoint.InputStreamSupplier;
import org.aksw.jena_sparql_api.io.endpoint.InputStreamSupplierBasic;
import org.apache.jena.ext.com.google.common.base.StandardSystemProperty;

import com.google.common.io.ByteStreams;

import io.reactivex.rxjava3.core.Single;

public class FilterExecutionFromSysFunction
    implements FilterConfig
{
    public static final Path PROBE_PATH = Paths.get("/tmp/probe");

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

    protected Path allocateInputFile() {
        Path result;
        try {
            // TODO Invoke callback
            //priorFilter.ifNeedsFileInput(pathRequester, processCallback)
            result = Files.createTempFile("highperfstream-input-", ".dat");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    protected Path allocateOutputFile() {
        Path result;
        try {
            // TODO Invoke callback
            //priorFilter.ifNeedsFileOutput(pathRequester, processCallback)
            result = Files.createTempFile("highperfstream-output-", ".dat");
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

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
                Path tmpOutFile = allocateOutputFile();
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

//	protected Single<HotFile> tryExecToHotFile(DestinationFromFileCreation d, Path tgtPath) {
//		//DestinationFromFileCreation d = (DestinationFromFileCreation)effectiveSource;
//		Single<HotFile> result = null;
//		Single<? extends FileCreation> fileCreation = d.getFileCreation();
//		Path inPath = d.getFileBeingCreated();
//
//		// Probe whether any combination works with the cmdFactory
//		String[] probeCmd;
//		if((probeCmd = cmdFactory.buildCmdForFileToFile(inPath, tgtPath)) != null) {
//		} else if((probeCmd = cmdFactory.buildCmdForFileToStream(inPath)) != null) {
//		}
//
//		if(probeCmd != null) {
//			result = waitForInputFileAndFilterToFile(fileCreation, tgtPath);
//		}
//
//		return result;
//	}

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
        Single<HotFile> result = null;

        Destination effectiveSource = getEffectiveSource();


        ProcessBuilder processBuilder = null;
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
            result = prepareHotFile(tgtPath, effectiveSource, processBuilder);
        }

        // If the processor could not handle stream input, try with a concrete file

        if(result == null) {
            // Depending on the destination, we can connect obtain the info
            // whether it will create a file upon requesting a stream
            // TODO We should have a common class for destinations that
            // "are or will be backed by a file"


            result = awaitOrAllocateInputFileAndFilterToFile(effectiveSource, tgtPath);
//
//			//result = tryExecToHotFile((DestinationFromFileCreation)effectiveSource, tgtPath);
//
//			if(effectiveSource instanceof DestinationFromFileCreation) {
//				result = tryExecToHotFile((DestinationFromFileCreation)effectiveSource, tgtPath);
//			} else if(effectiveSource instanceof DestinationFromFile) {
//				DestinationFromFile d = (DestinationFromFile)effectiveSource;
//				Path inPath = d.getPath();
//				Single<FileCreation> fileCreation = Single.just(new FileCreationWrapper(inPath));
//
//				result = waitForInputFileAndFilterToFile(fileCreation, inPath, tgtPath);

            if(result == null) {
                // We need a file but we were unable to obtain one from the destination
                // Attempt to stream the destination to a file first

                Path tmpInFile = allocateInputFile();

                // Probe whether any combination works with the cmdFactory
                String[] probeCmd;
                if((probeCmd = cmdFactory.buildCmdForFileToFile(tmpInFile, tgtPath)) != null) {
                } else if((probeCmd = cmdFactory.buildCmdForFileToStream(tmpInFile)) != null) {
                } else {
                    throw new RuntimeException("probing failed");
                }

                if(probeCmd != null) {
                    processBuilder = new ProcessBuilder(probeCmd);
                    // create a single that reads the input and forwards it to the process
                    processBuilder.redirectOutput(tgtPath.toFile());

                    result = prepareHotFile(tgtPath, effectiveSource, processBuilder);
                }
            }

        }

        return result;
    }


    private Single<HotFile> prepareHotFile(Path tgtPath, Destination effectiveSource, ProcessBuilder processBuilder) {
        Single<HotFile> result;
        result = effectiveSource.prepareStream().map(inSupp -> {
            Entry<Single<Integer>, Process> e = SimpleProcessExecutor.wrap(processBuilder).executeCore();
            Single<Integer> processSingle = e.getKey();
            Process process = e.getValue();

            // Copy the input into the process
            new Thread(() -> {
                try(InputStream in = inSupp.execStream().blockingGet()) {
                    try(OutputStream out = process.getOutputStream()) {
                        ByteStreams.copy(in, out);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).start();

            HotFile r = HotFileFromProcess.createStarted(tgtPath, processSingle);
            return r;
        });
        return result;
    }


    private Single<InputStreamSupplier> prepareStreamToStream(Destination effectiveSource, ProcessBuilder processBuilder) {
        Single<InputStreamSupplier> result;
        result = effectiveSource.prepareStream().map(inSupp -> {
            return () -> {
                Process process;
                try {
                    process = SimpleProcessExecutor.wrap(processBuilder).execute();
                } catch (InterruptedException e1) {
                    throw new RuntimeException(e1);
                }

                // Copy the input into the process
                new Thread(() -> {
                    try(InputStream in = inSupp.execStream().blockingGet()) {
                        try(OutputStream out = process.getOutputStream()) {
                            ByteStreams.copy(in, out);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }).start();

                return Single.just(process.getInputStream());
            };
        });
        return result;
    }


    public Single<? extends FileCreation> tryGetFileCreation(Destination destination) {
        Single<? extends FileCreation> result;

        if(destination instanceof DestinationFromFileCreation) {
            result = ((DestinationFromFileCreation)destination).getFileCreation();
        } else if(destination instanceof DestinationFromFile) {
            DestinationFromFile d = (DestinationFromFile)destination;
            Path inPath = d.getPath();
            result = Single.just(new FileCreationWrapper(inPath));
        } else {
            result = null;
        }

        return result;
    }


//	public Single<HotFile> awaitOrAllocateInputFileAndFilterToFile(Single<? extends FileCreation> fileCreation, Path inPath, Path tgtPath) {

    public Single<HotFile> awaitOrAllocateInputFileAndFilterToFile(Destination effectiveSource, Path tgtPath) {

        Single<? extends FileCreation> fileCreation = tryGetFileCreation(effectiveSource);
        if(fileCreation == null) {
            // TODO Ask the source to create the file
            // effectiveSource.
            fileCreation = forceInputFileCreation(effectiveSource);
        }

        if(fileCreation == null) {
            throw new RuntimeException("Should not happen");
        }

        Single<HotFile> result = awaitOrAllocateInputFileAndFilterToFileCore(fileCreation, tgtPath);
        return result;
    }

    public static Single<? extends FileCreation> forceDestinationToFile(Single<InputStreamSupplier> xxx, Path tmpFile) {
        ConcurrentFileEndpoint endpoint;
        try {
            endpoint = ConcurrentFileEndpoint.create(tmpFile, StandardOpenOption.CREATE);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        // TODO Hack
        return xxx
            .map(inSupp -> {
                new Thread(() -> {
                    try(InputStream in = inSupp.execStream().blockingGet()) {
                        ByteStreams.copy(in, Channels.newOutputStream(endpoint));
                    } catch(Exception e) {
                        endpoint.abandon();
                    } finally {
                        try {
                            endpoint.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }).start();

                //InputStreamSupplier r = Files.newInputStream(endpoint.getPath(), StandardOpenOption.READ);
                return new HotFileFromJava(endpoint, null);
            });
    }

    public Single<? extends FileCreation> forceInputFileCreation(Destination effectiveSource) {
        Single<? extends FileCreation> result;

        // Try to unwrap a file creation from the source
        result = tryGetFileCreation(effectiveSource);
        if(result == null) {

            // If we are in a filter pipe, request the prior filter to write to file
            if(effectiveSource instanceof DestinationFilter) {
                FilterConfig filter = ((DestinationFilter)effectiveSource).getFilter();

                Path tmpInFile = allocateInputFile();
                Destination tmpDest = filter.outputToFile(tmpInFile);
                result = tryGetFileCreation(tmpDest);
                if(result == null) {
                    throw new RuntimeException("Should not happen");
                }

            // Otherwise, obtain the destinations input stream and write it to file ourselves
            } else {
                Path tmpInFile = allocateInputFile();
                result = forceDestinationToFile(effectiveSource.prepareStream(), tmpInFile);
            }
        }


        if(result == null) {
            throw new RuntimeException("Could not force input to file");
        }

        return result;
    }

    public Single<HotFile> awaitOrAllocateInputFileAndFilterToFileCore(Single<? extends FileCreation> fileCreation, Path tgtPath) {

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


    public Single<InputStreamSupplier> awaitOrAllocateInputFileAndFilterToStream(Destination effectiveSource) {

        Single<? extends FileCreation> fileCreation = tryGetFileCreation(effectiveSource);
        if(fileCreation == null) {
            Path tmpInFile = allocateInputFile();
            fileCreation = Single.just(new FileCreationWrapper(tmpInFile));
        }

        String[] probeCmd;
        Single<InputStreamSupplier> result;
        if((probeCmd = cmdFactory.buildCmdForFileToStream(PROBE_PATH)) != null) {
            result = fileCreation.flatMap(fc -> {
                return Single.fromFuture(fc.future()).map(actualInPath -> {
                    String[] cmd = cmdFactory.buildCmdForFileToStream(actualInPath);
                    return () -> {
                        ProcessBuilder processBuilder;
                        processBuilder = new ProcessBuilder(cmd);
                        Entry<Single<Integer>, Process> processSingle;
                        try {
                            processSingle = SimpleProcessExecutor.wrap(processBuilder).executeCore();
                        } catch (InterruptedException e) {
                            throw new RuntimeException();
                        }
                        Process process = processSingle.getValue();

                        InputStream r = process.getInputStream();
                        return Single.just(r);
                    };
                });
            });

        } else if((probeCmd = cmdFactory.buildCmdForFileToFile(PROBE_PATH, PROBE_PATH)) != null) {

            Path outPath = allocateOutputFile();
            result = fileCreation.flatMap(fc -> {
                return Single.fromFuture(fc.future()).flatMap(actualInPath -> {
                    Single<InputStreamSupplier> r;
                    String[] cmd;
                    if((cmd = cmdFactory.buildCmdForFileToFile(actualInPath, outPath)) != null) {
                        Path outFile = allocateOutputFile();
                        r = execToHotFile(outFile).map(hotFile -> InputStreamSupplierBasic.wrap(hotFile::newInputStream));
                    } else {
                        throw new RuntimeException("cmdFactory could not cope with provided arguments");
                    }

                    //Single<Integer> processSingle = SimpleProcessExecutor.wrap(processBuilder).executeFuture();
                    return r;
                });
            });
        } else {
            result = null;
        }

        return result;
    }



//	public Single<HotFile> prepareInputAsFile() {
//
//		Path file;
//
//		if(source instanceof DestinationFromFileCreation) {
//			// Wait for the file to become ready
//			DestinationFromFileCreation d = (DestinationFromFileCreation)source;
//			Supplier<HotFile> supplier = d.getFileCreation();
//			try {
//				file = supplier.get().future().get();
//			} catch(InterruptedException | ExecutionException e) {
//				throw new IOException(e);
//			}
//		}
//
//		if(source instanceof DestinationFromFile) {
//			DestinationFromFile d = (DestinationFromFile)source;
//			file = d.getPath();
//		}
//
//
//		// Create a temporary file from the input
//		if(file == null) {
//			Single<InputStreamSupplier> streamCreation = source.execStream();
//			streamCreation.map(suppIn -> {
//				return (InputStreamSupplier)() -> {
//					try(InputStream in = suppIn.execStream()) {
//						// TODO Invoke the callback for a requested input file
//						//ifNeedsFileInput(pathRequester, processCallback)
//						Path tmpFile = Files.createTempFile("highperfstream", ".dat");
//
//						Files.copy(in, tmpFile);
//					};
//				};
//			});
//		}
//	}

    // input options subset of OPTS:= { stream, path}
    //
    // input option requirements (stream) -> foo, (path) > bar with foo, bar subseteq OPTS
    //
    // output input  output
    // stream stream stream
    //
    //
    //
    //
    //



    /**
     * Actually execute the filter
     *
     * Because we always prefer streams over files (which get passed as arguments), we check the
     * availability and applicability of commands of the cmdFactory in the following order:
     *
     * - stream to stream
     * - stream to file (we can stream the file being generated)
     * - file to stream
     * - file to file
     *
     *
     * the 'file to stream' cases will try to reuse files under generation in the source destination
     *
     *
     * Invokes ifNeedsFileInput and ifNeedsFileOutput handlers as needed
     *
     */
    @Override
    public Single<InputStreamSupplier> execStream() {
        Single<InputStreamSupplier> result;

        Destination effectiveSource = getEffectiveSource();

        ProcessBuilder processBuilder = null;
        String[] cmd;

        if((cmd = cmdFactory.buildCmdForStreamToStream()) != null) {
            processBuilder = new ProcessBuilder(cmd);
            result = prepareStreamToStream(effectiveSource, processBuilder);
        } else if((cmd = cmdFactory.buildCmdForStreamToFile(PROBE_PATH)) != null) {
            Path tmpOutFile = allocateOutputFile();
            result = execToHotFile(tmpOutFile).map(hotFile -> InputStreamSupplierBasic.wrap(hotFile::newInputStream));
        } else {

            result = awaitOrAllocateInputFileAndFilterToStream(effectiveSource);

            // We need a file as input - either reuse one being created
            // or write an input file
        }

        if(result == null) {
            throw new RuntimeException("Was not able to create a stream out of input");
        }

        return result;
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
        return new DestinationFromFileCreation(path,
                execToHotFile(path));
    }


    @Override
    public Destination outputToStream() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String toString() {
        return "FilterExecutionFromSysFunction [cmdFactory=" + cmdFactory + ", filterMetadata=" + filterMetadata
                + ", source=" + source + "]";
    }

}
