package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;

/**
 * A helper class to obtain process output as file or a stream
 *
 * @author raven
 *
 */
public class ProcessSinkImpl
    implements ProcessSink
{
    protected ProcessBuilder processBuilder;

    // Post start action, e.g. starting a thread to do copying
    protected Consumer<? super Process> postStart;

    protected Process process;

    public ProcessSinkImpl(ProcessBuilder processBuilder, Consumer<? super Process> postStart) {
        super();
        this.processBuilder = processBuilder;
        this.postStart = postStart;
    }

    protected synchronized Process startProcess() {
        if(process != null) {
            throw new RuntimeException("Process already started");
        }

        process = ProcessPipeUtils.startProcess(processBuilder);
        return process;
    }

    public InputStream getInputStream() {
        startProcess();
        postStart.accept(process);
        InputStream result = process.getInputStream();
        return result;
    }

    public FileCreation redirectTo(Path path) {
        processBuilder.redirectOutput(path.toFile());
        startProcess();
        postStart.accept(process);
        FileCreation result = ProcessPipeUtils.createFileCreation(process, path);
        return result;
    }
}