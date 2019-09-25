package org.aksw.commons.service.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.aksw.commons.utils.OmitSimilarItems;

import com.github.jsonldjava.shaded.com.google.common.collect.Maps;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;

public class SimpleProcessExecutor {
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
	            sink.accept("Process terminated with exit code " + exitValue);
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

    public Single<Integer> executeFuture() throws IOException, InterruptedException {
    	setService(true);
    	Single<Integer> result = executeCore().getKey();
    	return result;
    }

    public Entry<Single<Integer>, Process> executeCore() throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();


        Single<Integer> single = Flowable.<Integer>create(emitter -> {
    		if(isService) {
    			Thread thread = new Thread(() -> run(p, emitter));
                emitter.setCancellable(() -> {
                	System.out.println("Destroying process...");
                	p.destroy();
                	//p.destroyForcibly();
                	p.waitFor();
                	System.out.println("Done");
                	thread.interrupt();
                });
    			thread.start();
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
