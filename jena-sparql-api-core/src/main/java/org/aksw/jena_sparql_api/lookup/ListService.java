package org.aksw.jena_sparql_api.lookup;

import java.util.List;

public interface ListService<C, T> {
    List<T> fetchData(C concept, Long limit, Long offset);
    
    /**
     * Select Distinct ?v {
     *     { Select ?v {
     *         concept
     *     } Limit rawLimit }
     * } Limit resLimit
     * 
     * @param concept
     * @param itemLimit Limit applied on the set of distinct items (resources)
     * //@param rowLimit Limits the number of rows to scan before applying distinct 
     * @return
     */
    CountInfo fetchCount(C concept, Long itemLimit);
}
