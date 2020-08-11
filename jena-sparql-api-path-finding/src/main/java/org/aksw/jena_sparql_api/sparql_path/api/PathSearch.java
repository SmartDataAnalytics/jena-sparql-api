package org.aksw.jena_sparql_api.sparql_path.api;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import io.reactivex.rxjava3.core.Flowable;

/**
 * A {@link PathSearch} represents a search of paths under given parameters.
 * This interface provides methods to limit the number of results and their lengths.
 * Implementations may e.g. forward these parameters to the backing core algorithm -
 * or they may simply post-process a stream of paths by these conditions.
 *
 * @author Claus Stadler, Nov 11, 2018
 *
 * @param <P>
 */
public interface PathSearch<P> {

    default PathSearch<P> setMaxPathLength(Integer maxLength) {
        return setMaxLength(maxLength == null ? null : maxLength.longValue());
    }

    PathSearch<P> setMaxLength(Long maxLength);
    Long getMaxLength();

    default PathSearch<P> setMaxInternalResultsHint(Integer maxResults) {
        return setMaxInternalResultsHint(maxResults == null ? null : maxResults.longValue());
    }

    /**
     * Hint to the backing path finding algorithm about how many candidate paths to generate.
     * The hint may be ignored.
     *
     * @param maxResults
     * @return
     */
    PathSearch<P> setMaxInternalResultsHint(Long maxResults);
    Long getMaxInternalResultsHint();


    // PathSearch<P> setRandom(Random random);
    // PathSearch<P> setMaxLength(Long maxLength);

    default PathSearch<P> shuffle(Random random) {
        return transformInternal(f -> {
            List<P> list = f.toList().blockingGet();
            Collections.shuffle(list, random);
            return Flowable.fromIterable(list);
        });
    }

    default PathSearch<P> filter(Predicate<? super P> predicate) {
        //io.reactivex.functions.Predicate. p -> predicate.test(p);
        return transformInternal(f -> f.filter(predicate::test));
    }

    PathSearch<P> transformInternal(Function<Flowable<P>, Flowable<P>> filter);

    //PathSearch<P> transformInternal(Function<? super Flowable<? super P>, ? extends Flowable<? extends P>> filter);

    Flowable<P> exec();
}
