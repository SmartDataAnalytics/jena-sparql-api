package org.aksw.jena_sparql_api.utils;

import java.util.List;

public class ListUtils {
    /**
     * Return the item at index or null if it does not exist
     * @param list
     * @param i
     * @return
     */
    public static <T> T safeGet(List<T> list, int i) {
        T result = i >= list.size() ? null : list.get(i);
        return result;
    }
}
