package org.aksw.jena_sparql_api.io.pipe.process;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.io.endpoint.FileCreation;
import org.aksw.jena_sparql_api.io.filter.sys.ThrowingConsumer;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleTransformer;

public interface PipeTransform {
    default Function<InputStream, InputStream> mapStreamToStream() { return null; }
    default Function<Path, InputStream> mapPathToStream() { return null; }
    default BiFunction<Path, Path, FileCreation> mapPathToPath() { return null; }
    default BiFunction<InputStream, Path, FileCreation> mapStreamToPath() { return null; }
}


/**
 * RxWrapper for pipe transforms
 *
 * @author raven
 *
 */
class  PipeTransformRx {
    protected PipeTransform pipeTransform;

    public SingleTransformer<InputStream, InputStream> mapStreamToStream() {
        return pipeTransform.mapStreamToStream() == null ? null :
            upstream ->
                  upstream.map(pipeTransform.mapStreamToStream()::apply);
    }

//    default SingleTransformer<Path, InputStream> mapPathToStream() { return null; }
//    default SingleTransformer<Path, Path> mapPathToPath() { return null; }
    public SingleTransformer<InputStream, Path> mapStreamToPath() {
//        return pipeTransform.mapStreamToStream() == null ? null :
//            upstream ->
//                  upstream.flatMap(null);
        return null;
    }


    /**
     * If the file creation completed successfully, further subscriptions to the single
     * should return the cached path to the file instead of starting the creation again
     *
     *
     * @param fc
     * @return
     */
    public Single<Path> singeFromFileCreation(Supplier<FileCreation> fileCreationSupplier) {
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
                emitter.setCancellable(() -> cancelAction.accept(obj));

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
        .cache();
    }
}



