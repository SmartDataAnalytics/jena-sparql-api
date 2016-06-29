package org.aksw.isomorphism;

import java.util.stream.Stream;

public class StreamUtils {

    public static <T> Stream<T> appendAction(Stream<? extends T> stream, Runnable runnable) {
        Stream<T> result = Stream.concat(
                stream,
                Stream
                    .of((T)null)
                    .map(x -> {
                        runnable.run();
                        return x;
                    })
                    .filter(x -> x != null)
                );
        return result;
    }

}
