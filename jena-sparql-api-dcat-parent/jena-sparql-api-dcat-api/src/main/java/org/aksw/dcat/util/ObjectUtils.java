package org.aksw.dcat.util;

import java.util.Arrays;
import java.util.function.Supplier;

public class ObjectUtils {
    /**
     * Supplier-based coalesce function as described in
     * https://benjiweber.co.uk/blog/2013/12/08/null-coalescing-in-java-8/
     *
     * <br />
     * Example usage:
     * {@code coalesce(obj::getName, obj::getId, () -> obj.hasFoo() ? obj.foo().toString() : "bar") }
     *
     *
     * @param <T>
     * @param ts
     * @return
     */
    @SafeVarargs
    public static <T> T coalesce(Supplier<T>... ts) {
        return Arrays.asList(ts)
                .stream()
                .map(t -> t.get())
                .filter(t -> t != null)
                .findFirst()
                .orElse(null);
    }
}