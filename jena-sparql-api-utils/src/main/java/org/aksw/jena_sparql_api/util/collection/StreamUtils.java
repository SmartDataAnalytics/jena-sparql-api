package org.aksw.jena_sparql_api.util.collection;

import java.util.stream.Stream;

import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Streams;

public class StreamUtils {
    public static <T> Stream<T> stream(ClosableIterator<T> it) {
        Stream<T> result = Streams.stream(it);
        result.onClose(it::close);

        return result;
    }

    public static Stream<Binding> stream(QueryIterator it) {
        Stream<Binding> result = Streams.stream(it);
        result.onClose(it::close);

        return result;
    }

}
