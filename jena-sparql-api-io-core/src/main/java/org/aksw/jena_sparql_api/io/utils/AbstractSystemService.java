package org.aksw.jena_sparql_api.io.utils;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.aksw.commons.io.process.util.SimpleProcessExecutor;

import com.google.common.util.concurrent.AbstractIdleService;

public abstract class AbstractSystemService
    extends AbstractIdleService
{
//    private static final Logger logger = LoggerFactory.getLogger(AbstractSystemService.class);
    protected Duration healthCheckInterval = Duration.ofSeconds(3);

    protected transient Process process;
    protected Consumer<String> outputSink;

    public Consumer<String> getOutputSink() {
        return outputSink;
    }

    public AbstractSystemService setOutputSink(Consumer<String> outputSink) {
        this.outputSink = outputSink;
        return this;
    }

    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public AbstractSystemService setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
        return this;
    }

    // A single instance of the shutdown hook thread
    protected Thread shutdownHookThread = new Thread(() -> {
        // Not sure if we still have a logger during shutdown
        System.err.println("Shutdown hook: terminating virtuoso process");
        if(isRunning()) {
            stopAsync();
            try {
                awaitTerminated(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // Give up
                // TODO We could still try to destroyForcibly()
                throw new RuntimeException(e);
            }
        }
    });

    /**
     * Perform a health check on the service.
     * During a startup, a service enters running state in the moment
     * the health  check succeeds.
     *
     * @return
     */
    public abstract boolean performHealthCheck();

    /**
     * Expected to return a properly configured ProcessBuilder instance,
     * such as having the path to the executable and the arguments set.
     * @return
     */
    protected abstract ProcessBuilder prepareProcessBuilder();

    /**
     * Starts the service and yields a future indicating whether it became
     * healthy within a certain time limit.
     *
     * The service may still become healthy at a later stage, however,
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void startUp() throws IOException, InterruptedException {
        // Attempt to read the ini file

        Runtime.getRuntime().addShutdownHook(shutdownHookThread);

        ProcessBuilder pb = prepareProcessBuilder();
        process = SimpleProcessExecutor.wrap(pb)
                .setService(true)
                // Delegate output to whatever the current sink is
                .setOutputSink(x -> { if(outputSink != null) { outputSink.accept(x); }})
                .execute();

        // Some simple retry policy on the health check
        // TODO Use async retry library (or something similar) for
        // having a powerful api for crafting retry policies

        // Start another thread that determines when the service becomes healthy
        try {
            boolean r = false;
            // for(int i = 0; ; ++i) {
            while (process.isAlive()) {
                long millis = healthCheckInterval.toMillis();
                Thread.sleep(millis);
                r = performHealthCheck();
                if (r) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
            // If startup gets interrupted, shut down the service
            // throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutDown() {

        // Things may get better with Java 9, such as having an event when the process
        // terminates which would enable us to exit immediately without polling delay
        process.destroy();

        try {
            while (process.isAlive()) {
                // Waiting for process to die
                long millis = healthCheckInterval.toMillis();
                Thread.sleep(millis);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        process = null;
        Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
    }

}
