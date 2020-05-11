package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;
import org.aksw.jena_sparql_api.io.filter.sys.ThrowingConsumer;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.core.SingleTransformer;




/**
 * RxWrapper for pipe transforms
 *
 * @author raven
 *
 */
public class PipeTransformRx {
    protected PipeTransform pipeTransform;

    public PipeTransformRx(PipeTransform pipeTransform) {
        super();
        this.pipeTransform = pipeTransform;
    }

    public SingleTransformer<InputStream, InputStream> mapStreamToStream() {
        return pipeTransform.mapStreamToStream() == null ? null :
            upstream ->
                  upstream.map(pipeTransform.mapStreamToStream()::apply);
    }

//    default SingleTransformer<Path, InputStream> mapPathToStream() { return null; }
//    default SingleTransformer<Path, Path> mapPathToPath() { return null; }
    public SingleTransformer<InputStream, Path> mapStreamToPath(Path path) {
        return pipeTransform.mapStreamToPath() == null ? null :
            upstream ->
                  upstream.flatMap(in -> singleFromFileCreation(() -> pipeTransform.mapStreamToPath().apply(in, path)));
    }

    public SingleTransformer<Path, InputStream> mapPathToStream() {
        return pipeTransform.mapPathToStream() == null ? null :
            upstream ->
                  upstream.map(path -> pipeTransform.mapPathToStream().apply(path));
    }


    /**
     * If the file creation completed successfully, further subscriptions to the single
     * should return the cached path to the file instead of starting the creation again
     *
     *
     * @param fc
     * @return
     */
    public static Single<Path> singleFromFileCreation(Supplier<FileCreation> fileCreationSupplier) {
        return singleFromCompletableFuture(
                fileCreationSupplier,
                FileCreation::future,
                FileCreation::abort);
    }

    public static <T, V> Single<V> singleFromCompletableFuture(
            Supplier<T> objSupplier,
            Function<? super T, ? extends CompletableFuture<V>> getFuture,
            ThrowingConsumer<? super T> cancelAction) {
        return Single.create(new SingleOnSubscribe<V>() {
            @Override
            public void subscribe(SingleEmitter<V> emitter) throws Exception {
                T obj = objSupplier.get();
                emitter.setCancellable(() -> {
                    cancelAction.accept(obj);
                });

                CompletableFuture<V> future = getFuture.apply(obj);
                future.whenComplete((value, ex) -> {
                    if(ex != null) {
                        emitter.onError(ex);
                    } else {
                        emitter.onSuccess(value);
                    }
                });
            }
        })
        //.cache();
        ;
    }


    public static PipeTransformRx fromSysCallStreamToStream(String... args) {
//        SysCallPipeSpec spec = SysCallPipeSpec.fromCmdStreamToStream(args);
        PipeTransform pipeTransform = new PipeTransformSysCallStream(args);
        PipeTransformRx result = new PipeTransformRx(pipeTransform);
        return result;
    }
}
