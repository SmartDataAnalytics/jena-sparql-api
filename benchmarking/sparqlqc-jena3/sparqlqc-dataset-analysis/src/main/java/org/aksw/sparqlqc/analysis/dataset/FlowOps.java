package org.aksw.sparqlqc.analysis.dataset;

import org.aksw.jena_sparql_api.lookup.ListPaginator;
import org.aksw.jena_sparql_api.lookup.LookupService;

public class FlowOps {
    /**
     * Take chunkSize items from in, and pass them to the lookupservice
     *
     * @param chunkSize
     * @param in
     * @return
     */
    public static <I, O> ListPaginator<O> mapChunked(int chunkSize, LookupService<I, O> lookup) {
        return null;
    }
}
