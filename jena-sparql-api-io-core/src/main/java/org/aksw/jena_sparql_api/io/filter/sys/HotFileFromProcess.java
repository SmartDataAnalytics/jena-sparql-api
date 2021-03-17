package org.aksw.jena_sparql_api.io.filter.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.commons.io.process.util.SimpleProcessExecutor;
import org.aksw.jena_sparql_api.io.endpoint.ConcurrentFileReader;
import org.aksw.jena_sparql_api.io.endpoint.HotFile;

import com.google.common.base.Stopwatch;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HotFileFromProcess
    implements HotFile
{
    protected Path path;
    protected Channel writeChannel;


    // Calling disposable.dispose() should abort the process
    protected Disposable disposable;
    protected CompletableFuture<Path> resultFuture;


    public HotFileFromProcess(Path path, Single<Integer> processSingle) {
        super();
        init(path, processSingle);
    }

    /**
     * Calling this method will subscribe to the single (on Schedulers.io())
     * and thus start its attached process
     *
     * @param hotFile The file being written to
     * @param processSingle The single capturing the process
     * @return
     */
    public static HotFileFromProcess createStarted(Path hotFile, Single<Integer> processSingle) {
        HotFileFromProcess result = new HotFileFromProcess(hotFile, processSingle);
        return result;
    }

    public void abort() {
        disposable.dispose();
    }

    /**
     *
     * @param path The path being written to by the process
     * @param processSingle A cold single wrapping the process and yielding its exit code
     * @return
     */
    public void init(Path path, Single<Integer> processSingle) {

        this.path = path;
        this.resultFuture = new CompletableFuture<Path>();

        boolean[] isOpenFlag = { true };
        Disposable disposable = processSingle
            .subscribeOn(Schedulers.io())
            .doFinally(() -> isOpenFlag[0] = false)
            .subscribeWith(new DisposableSingleObserver<Integer>() {
                @Override
                public void onSuccess(Integer t) {
                    resultFuture.complete(path);
                }

                @Override
                public void onError(Throwable e) {
                    resultFuture.completeExceptionally(e);
                }
            });

        writeChannel = new Channel() {
            @Override
            public boolean isOpen() {
                boolean result = isOpenFlag[0];
                return result;
            }

            @Override
            public void close() throws IOException {
                disposable.dispose();
            }
        };


    }

    @Override
    public CompletableFuture<Path> future() {
        return resultFuture;
    }


    @Override
    public InputStream newInputStream() throws IOException {
        ReadableByteChannel tmp = ConcurrentFileReader.create(path, writeChannel, 100);
        InputStream result = Channels.newInputStream(tmp);
        return result;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        //SysCallFn fn = new SysCallFnLbZipEncode();

        Path src = Paths.get("/home/raven/Projects/Data/LSQ/deleteme.sorted.nt");
        Path tgt = Paths.get("/tmp/data.nt");
        //Files.createFile(tgt);

        //InputStream in = Files.newInputStream(src, StandardOpenOption.READ);
        //InputStream in = Files.newInputStream(tgt, StandardOpenOption.CREATE);
        String[] cmd = new String[] {"/bin/cp", src.toAbsolutePath().toString(), tgt.toAbsolutePath().toString() };//fn.buildCmdForStreamToFile(tgt);
        Single<Integer> single = SimpleProcessExecutor.wrap(new ProcessBuilder(cmd)).executeFuture();
        HotFile file = HotFileFromProcess.createStarted(tgt, single);
        Thread.sleep(1000);

        System.out.println("Here");
        file.future().whenComplete((path, t) -> System.out.println("File is ready!"));

        int numTasks = 1;
        int numWorkers = 4;

        List<Runnable> tasks = new ArrayList<>();
        for(int i = 0; i < numTasks; ++i) {
            tasks.add(
                () -> {
                    System.out.println("Thread #" + Thread.currentThread().getId() + ": " + "Started reader ");
                    String line;
                    BufferedReader br;
                    try {
                        br = new BufferedReader(new InputStreamReader(file.newInputStream()));
                        System.out.println("Thread #" + Thread.currentThread().getId() + ": " + br.lines().count());
//						while((line = br.readLine()) != null) {
//							System.out.println("Thread #" + Thread.currentThread().getId() + ": " + line);
//						}
                        System.out.println("Thread #" + Thread.currentThread().getId() + ": " + "Reader done");
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

    @Override
    public String toString() {
        return "HotFileFromProcess [path=" + path + "]";
    }
}
