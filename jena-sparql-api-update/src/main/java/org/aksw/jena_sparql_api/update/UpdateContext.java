package org.aksw.jena_sparql_api.update;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;


public class UpdateContext {
    // TODO Fill out this class

    private UpdateExecutionFactory updateExecutionFactory;
    private QueryExecutionFactory queryExecutionFactory;
    private int batchSize;

    private QuadContainmentChecker containmentChecker;

    /**
     * This attribute is used for containment checking of quads
     *
     */
    //private Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter;

//    public UpdateContext(UpdateExecutionFactory updateExecutionFactory, QueryExecutionFactory queryExecutionFactory, int batchSize, Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> filter) {
//        this.updateExecutionFactory = updateExecutionFactory;
//        this.queryExecutionFactory = queryExecutionFactory;
//        this.batchSize = batchSize;
//        this.filter = filter;
//    }

    public UpdateContext(UpdateExecutionFactory updateExecutionFactory, QueryExecutionFactory queryExecutionFactory, int batchSize, QuadContainmentChecker containmentChecker) {
        this.updateExecutionFactory = updateExecutionFactory;
        this.queryExecutionFactory = queryExecutionFactory;
        this.batchSize = batchSize;
        this.containmentChecker = containmentChecker;
    }

    public UpdateExecutionFactory getUpdateExecutionFactory() {
        return updateExecutionFactory;
    }
    public QueryExecutionFactory getQueryExecutionFactory() {
        return queryExecutionFactory;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public QuadContainmentChecker getContainmentChecker() {
        return containmentChecker;
    }

    public void setContainmentChecker(QuadContainmentChecker containmentChecker) {
        this.containmentChecker = containmentChecker;
    }


//    public Function<Diff<? extends Iterable<Quad>>, Diff<Set<Quad>>> getFilter() {
//        return filter;
//    }
}
