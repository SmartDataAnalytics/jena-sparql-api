package org.aksw.jena_sparql_api.io.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;

public class SimpleProcessExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleProcessExecutor.class);

    protected ProcessBuilder processBuilder;
    protected Consumer<String> outputSink;

    // Experimental; but tendency is to require users to apply similarity removal
    // on the sink themselves.
    protected UnaryOperator<Consumer<String>> similarityRemover;

    protected boolean isService;

    public SimpleProcessExecutor(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
        this.outputSink = System.out::println;
        this.similarityRemover = (dest) -> OmitSimilarItems.forStrings(6, dest);
    }

    public Consumer<String> getOutputSink() {
        return outputSink;
    }

    public SimpleProcessExecutor setOutputSink(Consumer<String> outputSink) {
        this.outputSink = outputSink;
        return this;
    }

    public UnaryOperator<Consumer<String>> getSimilarityRemover() {
        return similarityRemover;
    }

    public SimpleProcessExecutor setSimilarityRemover(UnaryOperator<Consumer<String>> similarityRemover) {
        this.similarityRemover = similarityRemover;
        return this;
    }

    public boolean isService() {
        return isService;
    }

    /**
     * Return the underlying processBuilder
     *
     * @return
     */
    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }

    /**
     * If the process is a service, its output will be processed by a separate thread.
     * Otherwise, all output will be consumed by the invoking thread.
     *
     * @return
     */
    public SimpleProcessExecutor setService(boolean isService) {
        this.isService = isService;
        return this;
    }

    /**
     * Utility function that blocks until the process the ends, thereby
     * forwarding output to the configured sink and applying filtering of
     * consecutive lines that are too similar
     *
     * @param p
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public int watchProcessOutput(Process p) throws IOException, InterruptedException {
//        try {
            Consumer<String> sink = similarityRemover == null
                    ? outputSink
                    : similarityRemover.apply(outputSink);


            int exitValue;
            try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {

                String line;
                while((line = br.readLine()) != null && !Thread.interrupted()) {
                    sink.accept(line);
                }

                exitValue = p.waitFor();
                //sink.accept("Process terminated with exit code " + exitValue);
            }
//        }
//        catch(IOException e) {
//            // Probably just the process died, so ignore
//        }
//        catch(Exception e) {
//            throw new RuntimeException(e);
//        }
            return exitValue;
    }

    /**
     * Wraps process execution as a single that will hold the exit code.
     * This which allows for waiting for the process to end as well as
     * canceling it using e.g. timeout
     *
     * @param p
     * @param emitter
     */
    public void run(Process p, FlowableEmitter<Integer> emitter) {
        try {
            int r = watchProcessOutput(p);
            emitter.onNext(r);
            emitter.onComplete();
        }catch(Exception e) {
            emitter.onError(e);
        }
    }

    public Process execute() throws IOException, InterruptedException {
        Process result = executeCore().getValue();
        return result;
    }

    public void executeReadLines(Flowable<String> upstream, FlowableEmitter<String> emitter) throws IOException {

        logger.debug("Starting process: " + processBuilder.command());

        processBuilder.redirectErrorStream(true);
        Process p;
        try {
            p = processBuilder.start();
        } catch (IOException e1) {
            emitter.onError(e1);
            return;
        }

        PrintStream out = new PrintStream(p.getOutputStream());
        InputStream in = p.getInputStream();

        Thread t = new Thread(() -> {

            //int exitValue;
            try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;

                boolean isInterrupted = false;
                while((line = br.readLine()) != null && !(isInterrupted = Thread.interrupted())) {
                    emitter.onNext(line);
                }

                if(!isInterrupted) {
                    p.waitFor();
                    emitter.onComplete();
                }
                emitter.onComplete();
                //sink.accept("Process terminated with exit code " + exitValue);
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        Callable<Void> closeAction = () -> {
            out.flush();
            out.close();
            return null;
        };

        emitter.setCancellable(() -> {
            closeAction.call();
        });

        upstream.subscribe(
                out::println,
                e -> {
                    p.destroy();
                    Exceptions.propagate(e);
                },
                closeAction::call);
    }

    public Single<Integer> executeFuture() throws IOException, InterruptedException {
        //setService(true);
        Single<Integer> result = executeCore().getKey();
        return result;
    }

    public Entry<Single<Integer>, Process> executeCore() throws IOException, InterruptedException {
        logger.debug("Starting process: " + processBuilder.command());


        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();


        Single<Integer> single = Flowable.<Integer>create(emitter -> {
            if(isService) {
                if(true) throw new RuntimeException("Do not use; use Single/Flowable.subscribeOn(Schedulers.io)");

                //Thread thread = new Thread(() -> run(p, emitter));
                emitter.setCancellable(() -> {
                    System.out.println("Destroying process...");
                    p.destroy();
                    //p.destroyForcibly();
                    p.waitFor();
                    System.out.println("Done");
                    //thread.interrupt();
                });
                //thread.start();
            } else {
                emitter.setCancellable(p::destroyForcibly);
                run(p, emitter);
            }
        }, BackpressureStrategy.BUFFER).firstOrError();

        return Maps.immutableEntry(single, p);
    }

    public static SimpleProcessExecutor wrap(ProcessBuilder processBuilder) {
        return new SimpleProcessExecutor(processBuilder);
    }
}
