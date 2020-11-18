package org.aksw.jena_sparql_api.io.filter.sys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.endpoint.Destination;
import org.aksw.jena_sparql_api.io.endpoint.DestinationFromFile;
import org.aksw.jena_sparql_api.io.endpoint.FileWritingProcess;
import org.aksw.jena_sparql_api.io.endpoint.FilterConfig;
import org.aksw.jena_sparql_api.io.endpoint.HotFile;
import org.apache.jena.rdf.model.Resource;

import io.reactivex.rxjava3.core.Single;

/**
 * Let's use IoEntity as an upper term for file or stream.
 * TODO Destination is the same thing right now.
 *
 * @author raven
 *
 */
interface IoEntity {
    Resource getMetadata();
    Destination getIoEntity();

}


/**
 * Configuration API for the conjunction of two io entity transforms
 *
 * @author raven
 *
 */
interface IoEntityTransformConjunctionBuilder {
    IoEntityTransformConjunctionBuilder preferIntermediaryFile();
    IoEntityTransformConjunctionBuilder preferStream();
    IoEntityTransformConjunctionBuilder forceIntermediaryFile();
    IoEntityTransformConjunctionBuilder forceStream();

    // If an intermediary file is generated,
    IoEntityTransformConjunctionBuilder waitForFileCompletion();

    // Configure a callback that gets invoked if execution of the conjunction
    // cretes an intermediary file
    FilterConfig onIntermediateFile(Supplier<Path> pathRequester, BiConsumer<Path, FileWritingProcess> processCallback);
    IoEntityTransform build();
}


interface IoEntityTransformConjunction
    extends IoEntityTransform {

}

class IoEntityTransformConjunctionBuilderImpl
    implements IoEntityTransformConjunctionBuilder, IoEntityTransform {


    protected IoEntityTransform lhs;
    protected IoEntityTransform rhs;

    public IoEntityTransformConjunctionBuilderImpl(IoEntityTransform lhs, IoEntityTransform rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public IoEntityTransformConjunctionBuilder preferIntermediaryFile() {
        return this;
    }

    @Override
    public IoEntityTransformConjunctionBuilder preferStream() {
        return this;
    }

    @Override
    public IoEntityTransformConjunctionBuilder forceIntermediaryFile() {
        return this;
    }

    @Override
    public IoEntityTransformConjunctionBuilder forceStream() {
        return this;
    }

    @Override
    public IoEntityTransformConjunctionBuilder waitForFileCompletion() {
        return this;
    }

    @Override
    public FilterConfig onIntermediateFile(Supplier<Path> pathRequester,
            BiConsumer<Path, FileWritingProcess> processCallback) {
        return null;
    }

    @Override
    public IoEntityTransform build() {
        return this;
    }

    @Override
    public IoEntityTransformConjunctionBuilder andThenConf(IoEntityTransform xform) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T accept(IoEntityTransformVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}



interface IoEntityTransformFactory {

}



/**
 * _ -xform1-> [ conjunction point ] -xform2->
 *
 * @author raven
 *
 */

interface IoEntityTransformVisitor<T> {
    T visit(IoEntityTransformBasic basic);
    T visit(IoEntityTransformNative nativ);
    T visit(IoEntityTransformConjunctionBuilder conjunction);
}


interface IoEntityTransform {
    // Create a conjunction from transforms
    default IoEntityTransformConjunctionBuilder andThenConf(IoEntityTransform xform) {
        return new IoEntityTransformConjunctionBuilderImpl(this, xform);
    }

    default IoEntityTransform andThen(IoEntityTransform xform) {
        return andThenConf(xform).build();
    }

    <T> T accept(IoEntityTransformVisitor<T> visitor);
}

interface IoEntityTransformBasic
    extends IoEntityTransform
{
    String getName();
}


interface IoEntityTransformNative
    extends IoEntityTransform
{
    // InputStream apply(InputStream in);


    default <T> T accept(IoEntityTransformVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}


interface IoEntityTransformCore {
    default Function<Path, InputStream> buildCmdForFileToStream(Path input) { return null; }
    default Function<InputStream, Path> buildCmdForStreamToFile(Path input) { return null; }
    default Function<InputStream, InputStream> buildCmdForStreamToStream() { return null; }
    default Function<Path, Path> buildCmdForFileToFile(Path input, Path output) { return null; }
}

class IoEntityTransformNativeImpl
    implements IoEntityTransformNative {

//	@Override
//	public TransformExecutionOutputBuilder forInput(Destination destination) {
//		// TODO Auto-generated method stub
//		return null;
//	}
}



interface ExecutionGraph {

}


interface ExecVisitor {

}

interface WorkflowExecutor
    extends ExecVisitor
{

}


/**
 * Compile a transformation workflow into something executable
 *
 * @author raven
 *
 */
class IoEntityTransformVisitorImpl
    implements IoEntityTransformVisitor<GenSource>
{
    protected GenSource currentSource;

    public IoEntityTransformVisitorImpl(GenSource currentSource) {
        this.currentSource = currentSource;
    }

    @Override
    public GenSource visit(IoEntityTransformBasic basic) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GenSource visit(IoEntityTransformNative nativ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GenSource visit(IoEntityTransformConjunctionBuilder conjunction) {
        // TODO Auto-generated method stub
        return null;
    }
}



class IoEntityTransformImpl
    implements IoEntityTransformBasic
{
    protected String name;

    public IoEntityTransformImpl(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(IoEntityTransformVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}


/**
 * A source is a becoming supplier of input streams.
 * A source may need to be generated first before it can start supplying those streams.
 * The future returned by getFuture() indicates when obtaining input streams is possible.
 *
 * In the special case that the source is a file being generated,
 * it is possibly to obtain a file view of it - the future of the file view only resolves when
 * file creation is complete.
 *
 * A source may be backed by a file that is being written to
 * and input streams are handed out during that process.
 *
 * In this case, a client may prefer to wait for file completion
 *
 * Note, that supplying input streams to a file that is not only append to but
 * also changed elsewhere is not meaningful.
 * In this case, the generating process should not yield a Source object directly, but wrap
 * the generation until its complete in a Single<Source>
 *
 *
 * @author raven
 *
 */
interface Source {
    boolean isOneTime(); // Some streams such as STDIN can only be consumed once
    boolean isConsumed(); // if true, getInputStream will fail
    InputStream getInputStream() throws Exception;

    boolean isUnderGeneration();
    void cancelGeneration() throws Exception;
    boolean getGenerationType(Class<?> clazz);
    CompletableFuture<?> getFuture();

    boolean isFileSource();
    SourceFromFile asFileSource();
}




class SourceFromInputStream
    extends SourceBase
    implements GenSource
{
    protected InputStream inputStream;
    protected boolean isConsumed;

    public SourceFromInputStream(InputStream inputStream) {
        super();
        this.inputStream = inputStream;
    }

    @Override
    public boolean isOneTime() {
        return true;
    }

    @Override
    public synchronized boolean isConsumed() {
        return isConsumed;
    }

    public synchronized InputStream getActualInputStream() {
        if(isConsumed) {
            throw new NoSuchElementException("InputStream was already consumed");
        }

        isConsumed = true;
        return inputStream;
    }

    @Override
    public boolean isUnderGeneration() {
        return false;
    }

    @Override
    public void cancelGeneration() {
    }

    @Override
    public boolean getGenerationType(Class<?> clazz) {
        return clazz.isAssignableFrom(this.getClass());
    }


    @Override
    public CompletableFuture<?> getFuture() {
        return CompletableFuture.completedFuture(this);
    }


    @Override
    public Single<Source> exec() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Single<SourceFromFile> execToFile(Path path) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}


class SourceFromHotFile
    extends SourceBase
{
    protected Path path; // The file being written to
//    protected AutoCloseable cancelAction;
//    protected CompletableFuture<Integer> isComplete;
    protected HotFile hotFile;

    public SourceFromHotFile(
            GenSource generator,
            HotFile hotFile) {
        super(generator);
    }

    @Override
    public boolean isOneTime() {
        return false;
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public InputStream getActualInputStream() throws IOException {
        return hotFile.newInputStream();
    }

    @Override
    public boolean isUnderGeneration() {
        return !hotFile.future().isDone();
    }

    @Override
    public void cancelGeneration() throws Exception {
        hotFile.abort();
    }

    @Override
    public boolean getGenerationType(Class<?> clazz) {
        return clazz.isAssignableFrom(SourceFromFile.class);
    }

    @Override
    public CompletableFuture<?> getFuture() {
        return hotFile.future();
    }


}

abstract class SourceBase
    implements Source
{
    protected GenSource generator;

    public SourceBase(GenSource generator) {
        super();
        this.generator = generator;
    }

    public SourceBase() {
        super();
    }

    @Override
    public InputStream getInputStream() throws Exception {
        getFuture().get();
        return getActualInputStream();
    }

    protected abstract InputStream getActualInputStream() throws Exception;

    @Override
    public boolean isFileSource() {
        return false;
    }

    @Override
    public SourceFromFile asFileSource() {
        return null;
    }
}

//class GenSourceFromInputStream
//    implements GenSource
//{
//    protected InputStream inputStream;
//    protected boolean isConsumed;
//
//    public GenSourceFromInputStream(InputStream inputStream) {
//        super();
//        this.inputStream = inputStream;
//    }
//
//    @Override
//    public GenSource getProvenance() {
//        return null;
//    }
//
//    @Override
//    public boolean isOneTime() {
//        return true;
//    }
//
//    @Override
//    public boolean isConsumed() {
//        return isConsumed;
//    }
//
//    @Override
//    public synchronized Single<Source> exec() {
//        isConsumed = true;
//        return Single.just(new SourceFromInputStream(inputStream, this));
//    }
//
//    @Override
//    public Single<SourceFromFile> execToFile(Path path) throws IOException {
//        return exec().map(source -> {
//            ConcurrentFileEndpoint endpoint = ConcurrentFileEndpoint.create(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
//            //OutputStream out = Channels.newOutputStream(endpoint);
//            CompletableFuture<Integer> future = new CompletableFuture<>();
//            // Create a thread for copying
//            Thread thread = new Thread(() -> {
//                try(InputStream in = source.getInputStream();
//                    OutputStream out = Channels.newOutputStream(endpoint)) {
//                        IOUtils.copyLarge(in, out);
//                    future.complete(0);
//                } catch(Exception e) {
//                    future.completeExceptionally(e);
//                }
//            });
//
//            // Interrupt the thread and close the streams
//            AutoCloseable dispose = () -> {
//                thread.interrupt();
//            };
//
//            HotFile hotFile = new HotFileFromJava(endpoint, dispose);
//
//            Source rx = new SourceFromHotFile(null, hotFile);
//            SourceFromFile r = null;
//            return r;
//        });
//    }
//
//}


abstract class SourceFromTransformBase
    extends SourceBase
{
    protected Source source;

    public SourceFromTransformBase(Source source) {
        super();
    }

    public Source getSource() {
        return source;
    }

    @Override
    public boolean isOneTime() {
        boolean result = source.isOneTime();
        return result;
    }

    @Override
    public boolean isConsumed() {
        boolean result = source.isConsumed();
        return result;
    }
}




class SysCallUtils {
    // Creating a pipe in java would involve creating a temprorary script* and invoking
    // it with the shell command -
    // either as arument to e.g. /bin/bash or in its own file
    // The pipe execution would have to take care of the resource management
    public static SysCallSpec pipe(SysCallSpec a, SysCallSpec b) {
        a.cmdBuilderStreamToStream();
        b.cmdBuilderStreamToStream();
        return null;
    }

//    public static Filter toFilter(SysCallSpec spec) {
//
//    }
}

interface Filter {
    InputStream transform(InputStream in);
}

interface Converter {
    // Result is a handle to cancel the process
    Object transform(Path in, Path out);
}

interface Src {
    InputStream newInputStream();
}

interface Sink {
    void exec(Path path);
    void exec(InputStream in);
}


/*
codec: transforms in input stream

converter: transforms a file


 */

//class SourceFromSysCallSpec
//    extends SourceFromTransformBase
//{
//    protected SysCallSpec sysCallSpec;
//
//    public SourceFromSysCallSpec(Source source, SysCallSpec sysCallSpec) {
//        super(source);
//        this.sysCallSpec = sysCallSpec;
//    }
//
//    @Override
//    public InputStream getInputStream() throws Exception {
////        InputStream raw = source.getInputStream();
////        InputStream result = encoder.apply(raw);
////        return result;
//    	return
//    }
//
//    @Override
//    public boolean isUnderGeneration() {
//        boolean result = source.isUnderGeneration();
//        return result;
//    }
//
//    @Override
//    public void cancelGeneration() {
//        // TODO Auto-generated method stub
//
//    }
//
//    @Override
//    public boolean getGenerationType(Class<?> clazz) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public CompletableFuture<?> getFuture() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    protected InputStream getActualInputStream() throws Exception {
//        // TODO Auto-generated method stub
//        return null;
//    }
//}



class SourceFromFile
    implements Source, GenSource
{
    protected Path path;

    // Provenance is the generator that created the file
    protected GenSource provenance;

    public SourceFromFile(Path path) {
        super();
        this.path = path;
    }

    @Override
    public boolean isOneTime() {
        return false;
    }

    @Override
    public boolean isConsumed() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    @Override
    public Single<Source> exec() {
        return Single.just(this);
    }


    @Override
    public boolean isFileSource() {
        return true;
    }

    @Override
    public SourceFromFile asFileSource() {
        return this;
    }

    @Override
    public boolean isUnderGeneration() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void cancelGeneration() {
    }

    @Override
    public boolean getGenerationType(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public Single<SourceFromFile> execToFile(Path path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<?> getFuture() {
        return CompletableFuture.completedFuture(this);
    }
}




/**
 * Factory for sources
 *
 *
 * @author raven
 *
 */
interface GenSource {
    // A file that now acts as a source may have been built by another process
    // that can be exposed as the provenance of this source
    // GenSource getProvenance();

    // SourceType getSourceType();
    // SourceType getPreferredType();




    boolean isOneTime(); // Some streams such as STDIN can only be consumed once
                         // Such plans can only be executed once
    boolean isConsumed(); // if true, a source was already created
    // InputStream getInputStream();

    /**
     * Build a new source object from the plan.
     * As soon as the single finishes, the source must be in a state
     * where Source.newInputStream() returns immediately.
     *
     * If the returned source is associated with the generation of
     * another source (e.g. a file)
     *
     * source.isGenerating(SourceType.class)
     * Single<Source> source.awaitFinished()
     *
     *
     * @return
     */
    Single<Source> exec();


    /**
     * Execute the plan to a given target file.
     *
     *
     * @param path
     * @return
     */
    Single<SourceFromFile> execToFile(Path path) throws Exception;
}


//class GenSourceFromFile
//    implements GenSource
//{
//    protected GenSource provenance;
//    protected Path path;
//
//    @Override
//    public boolean isOneTime() {
//        return false;
//    }
//
//    @Override
//    public boolean isConsumed() {
//        return false;
//    }
//
//    @Override
//    public Single<Source> exec() {
//        return Single.just(new SourceFromFile(path));
//    }
//
//    @Override
//    public GenSource getProvenance() {
//        return provenance;
//    }
//}

abstract class GenSourceTransformBase
    implements GenSource
{
    protected GenSource source;

    public GenSourceTransformBase(GenSource source) {
        this.source = source;
    }


    @Override
    public boolean isOneTime() {
        boolean result = source.isOneTime();
        return result;
    }

    @Override
    public boolean isConsumed() {
        boolean result = source.isConsumed();
        return result;
    }

}

// Transform that upon execution writes a stream to a file
class GenSourceToFile
    extends GenSourceTransformBase
{
    public GenSourceToFile(GenSource source) {
        super(source);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isOneTime() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isConsumed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Single<Source> exec() {
        // TODO Auto-generated method stub
        return null;
    }

    //@Override
    public Single<SourceFromFile> exec(Path file) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Single<SourceFromFile> execToFile(Path path) {
        // TODO Auto-generated method stub
        return null;
    }

}


// Factory to create transforms that may make use of intermediate files
interface GenSourceTransformFactory {
    Function<GenSourceApplyTransform, GenSourceApplyTransform> create();


    // This can wrap a system call such as my-cmd -i foo -o bar
    // The awkward case that should never happen would be:
    // for an input file the output is streaming
    // for an input stream the output must be a file
    boolean requiresFileInput();
    boolean requiresFileOutput();
}


class GenSourceApplyTransform
    extends GenSourceTransformBase
{
    protected Function<InputStream, InputStream> encoder;

    public GenSourceApplyTransform(GenSource source, Function<InputStream, InputStream> encoder) {
        super(source);
        this.encoder = encoder;
    }

    @Override
    public Single<Source> exec() {
        return null;
        // return source.exec().map(source -> new SourceFromTransform(source, this, encoder));
    }

    @Override
    public Single<SourceFromFile> execToFile(Path path) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}



interface Endpoint
    extends Source, Sink
{

}

interface EndpointFromFile
    extends Endpoint
{

}



// Materialize a stream to a file
// The stream may not be repeatable readable (e.g. STDIN)
class OpStreamToFile {
    Path file;
    Destination apply(Destination entity) {
        return null;
    }
}

class OpFileToStream {
    Path file;
    Destination apply(Destination entity) {
        return null;
    }
}




interface TransformExecutionFactory {
    TransformExecutionInputBuilder executionFor(IoEntityTransform xform);
}


class TransformExecutionFactoryImpl
    implements TransformExecutionFactory
{
    public TransformExecutionInputBuilder executionFor(IoEntityTransform xform) {
        return null;
    }
}


interface TransformExecutionInputBuilder {
    Source exec();

    default TransformExecutionOutputBuilder bindToInput(Path path) {
        return bindToInput(new DestinationFromFile(path));
    }
    //TransformExecutionOutputBuilder forInput(IoEntity entity);
    TransformExecutionOutputBuilder bindToInput(Destination destination);
}



class TransformExecutionInputBuilderImpl
    implements TransformExecutionInputBuilder, TransformExecutionOutputBuilder
{
    protected Source source;
    protected IoTransform transform;

    @Override
    public Single<Source> execToDefault(Supplier<Path> fileRequiredCallback) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Single<Source> execToFile(Path outfile) {



        return null;
    }

    @Override
    public TransformExecutionOutputBuilder bindToInput(Destination destination) {
        this.source = null;
        return this;
    }

    @Override
    public Source exec() {
        // TODO Auto-generated method stub
        return null;
    }
}


enum SourceType {
    STREAM,
    FILE, // A completed / staic file
    HOT_FILE // A file being written to
}

interface Spec {

}

interface SpecBuilder {
    Spec buildFor(SourceType inputType, SourceType outputType);
}


interface CmbBuilderFileToStream {
    String[] build(Path inPath);
}

interface CmbBuilderStreamToFile {
    String[] build(Path outPath);
}

interface CmbBuilderStreamToStream {
    String[] build();
}

interface CmbBuilderFileToFile {
    String[] build(Path inPath, Path outPath);
}


/**
 * This is just the plain command construction - it does not involve operator metadata
 * whether e.g. a file must be completely rewritten before the transformation can be applied
 *
 * @author raven
 *
 */
interface SysCallSpec {
    default CmbBuilderStreamToStream cmdBuilderStreamToStream() { return null; }
    default CmbBuilderStreamToFile cmdBuilderStreamToFile() { return null; }
    default CmbBuilderFileToStream cmdBuilderFileToStream() { return null; }
    default CmbBuilderFileToFile cmdBuilderFileToFile() { return null; }
}


class SpecBuilderSysCall
    implements SpecBuilder2
{
    protected SysCallSpec core;

    public SpecBuilderSysCall(SysCallSpec core) {
        super();
        this.core = core;
    }

    @Override
    public SpecStreamToStream streamToStream() {
        // core.buildCmdForStreamToFile(input)
        return null;
    }

    @Override
    public SpecPathToStream pathToStream() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpecStreamToPath streamToPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpecPathToPath pathToPath() {
        // TODO Auto-generated method stub
        return null;
    }

}

interface SpecBuilder2 {
    SpecStreamToStream streamToStream();
    SpecStreamToPath streamToPath();
    SpecPathToStream pathToStream();
    SpecPathToPath pathToPath();
}

interface SpecPathToStream {
    default boolean allowIncompleteInput() { return false; }
    Single<InputStream> apply(Path path);
}

interface SpecStreamToStream {
    Single<InputStream> apply(Path path);
}

interface SpecStreamToPath {
    Single<Path> apply(InputStream in);
}

interface SpecPathToPath {
    default boolean allowIncompleteInput() { return false; }
    Single<Integer> apply(Path in, Path out);
}



interface TransformExecutionOutputBuilder {
    Single<Source> execToDefault(Supplier<Path> fileRequiredCallback);
    // Single<InputStream> execToStream();

    Single<Source> execToFile(Path outfile);
//    Destination forOutput(Path outFile);
//    Destination forStreamOutput();
}

//interface TransformExecution {
//    Destination exec();
////    void abort();
////    IoEntity getEntity();
//}
//



// The execution of a transform is an EntityCreation
// The entity can be a stream, a file or a hotfile
// Hm: Invoking newInputStream should probably be capable of repeating the whole workload
interface SomeEntity {
    // If the entity is a process, then that process may get executed for each invocation
    // in other cases, the input stream may simply get wrapped
    InputStream newInputStream();
}


//class TransformExecutionFactoryImpl
//    implements TransformExecutionFactory
//{
//    @Override
//    public TransformExecutionInputBuilder executionFor(IoEntityTransform xform) {
//        return new TransformExecutionInputBuilderImpl(xform);
//    }
//}


//class TransformExecutionInputBuilderImpl
//    implements TransformExecutionInputBuilder,TransformExecutionOutputBuilder
//{
//    protected Destination input;
//    protected IoEntityTransform xform;
//
//    public TransformExecutionInputBuilderImpl(IoEntityTransform xform) {
//        super();
//        this.xform = xform;
//    }
//
//    @Override
//    public TransformExecutionOutputBuilder bindToInput(Destination destination) {
//        input = destination;
//        return this;
//    }
//
//    @Override
//    public Single<Source> execToFile(Path outFile) {
//        // Create a destination that upon prepareStream writes the outFile
//        return null;
//    }
//
//
//    @Override
//    public Single<Source> execToDefault(Supplier<Path> fileRequiredCallback) {
//        // TODO Auto-generated method stub
//        return null;
//    }
////    @Override
////    public Destination forStreamOutput() {
////        return null;
////    }
//
//}


// A single inputstream with information about resources such as system processes that power it
interface InputStreamGeneration
//    implements InputStream
{

}

//interface SourceCreationExecution {
//    CompletableFuture<InputStream> getFuture();
//}


class Sources {
    public static Source fromFile(Path path) {
        return new SourceFromFile(path);
    }

    public static Source fromInputStream(InputStream inputStream) {
        return new SourceFromInputStream(inputStream);
    }
}


class GenSources {
    public static GenSource fromFile(Path path) {
        return new SourceFromFile(path);
    }

    public static GenSource fromInputStream(InputStream inputStream) {
        return new SourceFromInputStream(inputStream);
    }

    public static GenSourceTransform sysCallTransform(SysCallSpec spec) {
        return new SourceTransformFromSysCallSpec(spec);
    }
}


interface GenSourceTransform {
    GenSource apply(GenSource source);
}


class GenSourceFromSysCallSpec
    implements GenSource
{
    protected GenSource from;
    protected SysCallSpec sysCallSpec;

    @Override
    public boolean isOneTime() {
        return from.isOneTime();
    }

    @Override
    public boolean isConsumed() {
        return from.isConsumed();
    }

    @Override
    public Single<Source> exec() {
        // TODO Auto-generated method stub
        return null;
    }


    public Single<Source> execToStream() {
        //

        sysCallSpec.cmdBuilderStreamToStream();

        return null;
    }

    @Override
    public Single<SourceFromFile> execToFile(Path path) throws Exception {



        // TODO Auto-generated method stub
        return null;
    }

}


interface PipeBuilder {
    PipeBuilder add(String opName);

    // buildFor()

    default void example() {
        this
            .add("bz2-decode")
               //.onFile()
            .add("gz2-encode")
            //
            ;


    }

}

class SourceTransformFromSysCallSpec
    implements GenSourceTransform
{
    protected SysCallSpec sysCallSpec;

    public SourceTransformFromSysCallSpec(SysCallSpec sysCallSpec) {
        super();
        this.sysCallSpec = sysCallSpec;
    }

    @Override
    public GenSource apply(GenSource source) {
        //return new GenSourceFromSysCallSpec(source, sysCallSpec);
        return null;
    }
}



public class IoTransform {


    public static void mainOld(String[] args) throws IOException {
        IoEntityTransform bz2Decode = new IoEntityTransformImpl("bz2Decode");
        IoEntityTransform gz2Encode = new IoEntityTransformImpl("gz2Encode");

        SysCallSpec encodeBzipCore = new SysCallSpec() {
            @Override
            public CmbBuilderFileToStream cmdBuilderFileToStream() {
                return new CmbBuilderFileToStream() {
                    @Override
                    public String[] build(Path input) {
                        return new String[] {"/usr/bin/lbzip2", "-czk", input.toString()};
                    }
                };
            }
        };

        SpecBuilder2 specBzip2 = new SpecBuilderSysCall(encodeBzipCore);

        // Make an abstract workflow concrete
        TransformExecutionFactory executionFactory = new TransformExecutionFactoryImpl();
        TransformExecutionInputBuilder e = executionFactory.executionFor(null);

        Path input = Paths.get("/home/raven/Projects/Eclipse/hobbit-wp6/6_2_Faceted_Browsing/Deliverable_6_2_2/data/Output_2.1M_sensors.rdf.bz2");


        Source src = Sources.fromFile(input);
        System.out.println(src.getFuture().isDone());


        // SourceTransform xform = Sources.sysCallTransform(specBzip2);


        //SourceCreationExecution exec = e.bindToInput(input).exec();

        //monitor(exec, exec.getFuture());



        // specBzip2


        // How to report waiting for a file?
        // We may need an execution graph (faraday-cage from deer comes to mind)



        System.out.println("Hi");

        if(false) {

        // The transform is just a specification; it does not execute anything so far
        IoEntityTransform compositeTransform = bz2Decode.andThenConf(gz2Encode)
            .preferIntermediaryFile()
            // .onDisposeOfIntermediateFile() // By default, intermeddite files are deleted; this way they can be backed up
            //.onIntermediateFile(pathRequester, processCallback)
            .build();




        Path output = null;
        Single<Source> destination = executionFactory.executionFor(compositeTransform)
            .bindToInput(input)
            .execToFile(null);
            //.forOutput(output)
            //.materialize(null);
            ;


        //InputStream in = destination.prepareStream().blockingGet().execStream().blockingGet();
        }

    }



    /**
     * Monitor status of an object until a completable future
     * completes
     *
     * @param obj
     * @param future
     */
    public static void monitor(Object obj, CompletableFuture<?> future) {
        Thread wakeup = Thread.currentThread();
        future.whenComplete((v, t) -> wakeup.interrupt());
        while(!future.isDone()) {
            System.out.println(obj);

            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                // Silently ignore
            }
        }
    }
}
