package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.google.common.io.ByteStreams;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

// TODO Consolidate with AkswExceptionUtils and ExceptionUtils in NGS...
class ExceptionUtils2 {

    public static boolean isClosedChannelException(Throwable t) {
        boolean result = t instanceof ClosedChannelException;
        return result;
    }

    @SafeVarargs
    public static void rethrowUnlessRootCauseMatches(
            Throwable e,
            Consumer<? super Predicate<? super Throwable>> firstMatchingConditionCallback,
            Predicate<? super Throwable>... conditions) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);

        Predicate<? super Throwable> match = Arrays.asList(conditions).stream()
            .filter(p -> p.test(rootCause))
            .findFirst()
            .orElse(null);

        if(match == null) {
            throw new RuntimeException(e);
        } else {
            firstMatchingConditionCallback.accept(match);
        }
    }

    @SafeVarargs
    public static void rethrowUnlessRootCauseMatches(Throwable e, Predicate<? super Throwable>... conditions) {
        rethrowUnlessRootCauseMatches(e, condition -> {}, conditions);
    }
}


/**
 * Utility functions to create file and input stream transform based on system calls
 *
 * InputStreams <b>passed as arguments</b> to the various processors are considered to be owned by the
 * processors. Hence, the processors are in charge of closing them.
 * The client code is in charge of closing InputStreams returned by the processors.
 *
 * TODO Enhance the FileCreation objects such that they expose a future for when
 * the process started modifying the file.
 *
 * @author raven
 *
 */
public class ProcessPipeUtils {

    /**
     * TODO Convert to test cases
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (false) {
            RandomAccessFile f = new RandomAccessFile("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt", "r");
            String line;
            while((line = f.readLine()) != null) {
                System.out.println(line);
            }
        }

        if (false) {
            PipeTransform pt = null;
            Path src = null;
            Path tgt = null;

            // This is an example of how the API is still bad:
            // the future.get() blocks the input stream creation and we have no way to cancel it
            // We want a way that immediately returns an object from which the input stream can be obtained once it is ready
            // Obviously, a Single<InputStream> would be much better
            InputStream in = pt.mapPathToPath()
                    .andThen(fc -> {
                        try {
                            return pt.mapPathToStream().apply(fc.future().get());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .apply(src, tgt);


        }

        if (true) {
            // This already looks a lot better.

            // But now: how can we delete intermediate files if we chain
            // non-streaming operations?
            // Do we need a close() method on a Pipeline object?
//            PipelineRx
//                .newPipeline()
//                .whatnow();

            //

            Path ntFile = Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt");

            PipeTransformRx cat = PipeTransformRx.fromSysCallStreamToStream("/bin/cat");
            PipeTransformRx sort = PipeTransformRx.fromSysCallStreamToStream("/usr/bin/sort");
            PipeTransformRx filter = PipeTransformRx.fromSysCallStreamToStream("/bin/grep", "size");

            //InputStream tmp =
            Disposable disposable =
                    Single.just(ntFile)
                        .compose(cat.mapPathToStream())
                        .compose(sort.mapStreamToPath(Paths.get("/tmp/foo.bar")))
                        .compose(filter.mapPathToStream())
                        //.timeout(10, TimeUnit.SECONDS)
                        //.blockingGet();
                        .subscribe();

            disposable.dispose();
//            String line = new BufferedReader(new InputStreamReader(tmp)).readLine();
//            tmp.close();
//            System.out.println(line);

            //ByteStreams.copy(tmp, System.out);;

        }


        Path ntFile = Paths.get("/home/raven/Projects/Eclipse/blank-node-survey-parent/output.nt");
        if (false) {
            Function<InputStream, InputStream> xform = ProcessPipeUtils.createPipedTransformer((in, out) -> {
                Model model = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model, in, Lang.NTRIPLES);
                RDFDataMgr.write(out, model, Lang.TURTLE);
            });
            // OutputStream out = new CloseShieldOutputStream(System.out);
            try(InputStream in = Files.newInputStream(ntFile, StandardOpenOption.READ)) {
                in.close();
                ByteStreams.copy(xform.apply(in), System.out);
            }
        }

        if (false) {
            Function<InputStream, InputStream> xform = ProcessPipeUtils
                    .mapStreamToStream(new String[] { "/usr/bin/sort" })
                    .asStreamTransform();
            try(InputStream in = Files.newInputStream(ntFile, StandardOpenOption.READ)) {
                InputStream in2 = xform.apply(in);

                String line = new BufferedReader(new InputStreamReader(in2)).readLine();
                in2.close();
                System.out.println(line);

                //ByteStreams.copy(in2, System.out);
            }
        }

        if (false) {
            Function<Path, InputStream> xform = ProcessPipeUtils
                    .mapPathToStream(path -> new String[] { "/bin/cat", path.toString() })
                    .asStreamSource();
            try(InputStream in = xform.apply(ntFile)) {
                String line = new BufferedReader(new InputStreamReader(in)).readLine();
                System.out.println(line);

                //ByteStreams.copy(in2, System.out);
            }
        }

        if (false) {
            BiFunction<Path, Path, FileCreation> xform = ProcessPipeUtils.mapPathToPath((src, tgt) -> new String[] { "/bin/cp", src.toString(), tgt.toString() });

//            FileCreation fc = xform.apply(Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt"), Paths.get("/tmp/copy.nt"));
//            fc.abort();

            FileCreation fc = xform.apply(ntFile, Paths.get("/tmp/copy.nt"));
            Path tmp = fc.future().get();
            System.out.println("File creation done: " + tmp);
        }

    }


    /**
     * TODO Move this method elsewhere as it does not make use of a process
     *
     *
     * Transformation using a piped input/outputstream
     * createPipedTransform((in, out) -> { for(item : () -> readItems(in)) { write(item); } )
     *
     *
     * @param action
     * @return
     */
    public static Function<InputStream, InputStream> createPipedTransformer(BiConsumer<InputStream, OutputStream> action) {
        return in -> {
            PipedOutputStream pout = new PipedOutputStream();
            PipedInputStream pin;
            try {
                pin = new PipedInputStream(pout);
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            new Thread(() -> {
                try(OutputStream tmpOut = pout) {
                    action.accept(in, tmpOut);
                } catch (Exception e) {
                    ExceptionUtils2.rethrowUnlessRootCauseMatches(e,
                            match -> {
                                /* Silently ignore, because closing the channel is valid */
                                // TODO Add logger just in case
                                System.err.println("[DEBUG] Channel closed prematurely");
                            },
                            ExceptionUtils2::isClosedChannelException);

                }
            }).start();
            return pin;
        };
    }

    /**
     * Create a new thread to copy from source to target
     * TODO Closing the target silently terminate the thread and associated
     * resources (such as a system process)
     *
     * Closing the input stream should not happen though as it is
     * considered to be owned by the copy process
     *
     * @param from
     * @param to
     * @return
     */
    public static Thread startThreadedCopy(InputStream from, OutputStream to, Consumer<Exception> failureCallback) {
        Thread result = new Thread(() -> {
            try(InputStream in = from; OutputStream out = to) {
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                failureCallback.accept(e);
                throw new RuntimeException(e);
            }
        });

        result.start();
        return result;
    }


    public static Process startProcess(ProcessBuilder processBuilder) {
        Process result;
        try {
            result = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static PathToStream mapPathToStream(Function<Path, String[]> cmdBuilder) {
        return path -> {
            String[] cmd = cmdBuilder.apply(path);
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            ProcessSink r = new ProcessSinkImpl(processBuilder, p -> {});
            return r;
        };
    }

    public static FileCreation createFileCreation(Process process, Path path) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            try {
                process.waitFor();
                int exitValue = process.exitValue();
                if(exitValue == 0) {
                    future.complete(path);
                } else {
                    future.completeExceptionally(new RuntimeException("Process ended with non-zero exit code " + exitValue));
                }

            } catch(InterruptedException e) {
                // If this thread for whatever reason dies, try to kill the process
                process.destroy();
                future.completeExceptionally(e);
            } catch(Exception e) {
                future.completeExceptionally(e);
            }
        });
        thread.start();

        FileCreation r = new FileCreation() {
            @Override
            public CompletableFuture<Path> future() {
                return future;
            }

            @Override
            public void abort() throws Exception {
                process.destroy();
            }
        };

        return r;
    }

    public static BiFunction<InputStreamOrPath, Path, FileCreation> mapStreamToPath(Function<Path, String[]> cmdBuilder) {
        return (src, tgt) -> {
            String[] cmd = cmdBuilder.apply(tgt);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);

            if(src.isPath()) {
                processBuilder.redirectInput(src.getPath().toFile());
            }

            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            OutputStream out = process.getOutputStream();

            if(!src.isPath()) {
                // If the process dies, the copy thread dies anyway
                // But if the copy thread dies, the process may still be alive
                // Hence destroy the process on copy thread death
                startThreadedCopy(src.getInputStream(), out, e -> process.destroy());
            }

            FileCreation r = createFileCreation(process, tgt);
            return r;
        };
    }

    public static BiFunction<Path, Path, FileCreation> mapPathToPath(BiFunction<Path, Path, String[]> cmdBuilder) {
        return (src, tgt) -> {
            String[] cmd = cmdBuilder.apply(src, tgt);

            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            Process process;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            FileCreation r = createFileCreation(process, tgt);
            return r;
        };
    }

    public static StreamToStream mapStreamToStream(String[] cmd) {
        return src -> {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            if(src.isPath()) {
                processBuilder.redirectInput(src.getPath().toFile());
            }

            ProcessSink r = new ProcessSinkImpl(processBuilder, p -> {
                if(!src.isPath()) {
                    OutputStream out = p.getOutputStream();
                    startThreadedCopy(src.getInputStream(), out, e -> p.destroy());
                }
            });

            return r;
        };
    }

    // TODO Variants that use redirects
    // Streaming variants may in addition redirect file to process
    // So we need a builder object in between

}

