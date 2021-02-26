package org.aksw.jena_sparql_api.conjure.entity.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

import org.aksw.commons.io.process.util.SimpleProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public abstract class PathCoderSysBase
    implements PathCoder
{
    private static final Logger logger = LoggerFactory.getLogger(PathCoderSysBase.class);

    protected abstract String[] buildCheckCmd();
    protected abstract String[] buildDecodeCmd(Path input);
    protected abstract String[] buildEncodeCmd(Path input);

    public boolean cmdExists() {

        String[] cmd = buildCheckCmd();

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);

        boolean result;
        try {
            logger.trace("Checking availability of system command such as 'lbzip2'");
            Process p = SimpleProcessExecutor.wrap(processBuilder)
                .setOutputSink(logger::trace)
                .execute();

            int exitValue = p.exitValue();
            result = exitValue == 0;
        } catch(Exception e) {
            logger.debug("System command 'lbzip2' not available", e);
            result = false;

        }
        return result;
    }

    @Override
    public Single<Integer> decode(Path input, Path output) {
        return run(input, output, this::buildDecodeCmd);
    }

    @Override
    public Single<Integer> encode(Path input, Path output) {
        return run(input, output, this::buildEncodeCmd);
    }

//	public Entry<Process, Single<Integer>> exec(ProcessBuilder processBuilder) {
//        processBuilder.redirectErrorStream(true);
//        Process p = processBuilder.start();
//
//
//        Single<Integer> result;
//        if(isService) {
//            Single.fromCallable(() -> 1)
//
//
//        	new Thread(() -> watchProcessOutput(p)).start();
//        } else {
//            watchProcessOutput(p);
//        }
//
//        return p;
//
//	}
//
    public Single<Integer> run(Path input, Path output, Function<Path, String[]> buildCmd) {
//		String i = input.toString().replace("'", "\\'");
//		String o = output.toString().replace("'", "\\'");

        String[] cmd = buildCmd.apply(input);

        // System.out.println(Arrays.asList(cmd));

        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        File outFile = output.toFile();
        processBuilder.redirectOutput(outFile);
        SimpleProcessExecutor x = SimpleProcessExecutor.wrap(processBuilder);
//         	.setOutputSink(logger::info);

        //System.out::println) //logger::debug)
        Single<Integer> result;
        try {
            result = x.executeFuture().subscribeOn(Schedulers.io());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
//		try {
//			result = SimpleProcessExecutor.wrap(processBuilder)
//			    .setOutputSink(logger::info)
//			    .executeFuture();
//		} catch (IOException | InterruptedException e) {
//			throw new RuntimeException(e);
//		}
        return result;

//		x.getProcessBuilder().

//        Single.fromCallable(() -> {
//        	x.execute();
//        })
//        CompletableFuture<?> result = CompletableFuture.runAsync(runnable);
    }
}
