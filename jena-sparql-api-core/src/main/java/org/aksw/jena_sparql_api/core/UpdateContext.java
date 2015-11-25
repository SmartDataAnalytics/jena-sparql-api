package org.aksw.jena_sparql_api.core;



public class UpdateContext {
    // TODO Fill out this class

//    private UpdateExecutionFactory updateExecutionFactory;
//    private QueryExecutionFactory queryExecutionFactory;
    private SparqlService sparqlService;
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
// UpdateExecutionFactory updateExecutionFactory, QueryExecutionFactory queryExecutionFactory,
    public UpdateContext(SparqlService sparqlService, int batchSize, QuadContainmentChecker containmentChecker) {
        this.sparqlService = sparqlService;
        //this.updateExecutionFactory = updateExecutionFactory;
        //this.queryExecutionFactory = queryExecutionFactory;
        this.batchSize = batchSize;
        this.containmentChecker = containmentChecker;
    }

    public SparqlService getSparqlService() {
        return this.sparqlService;
    }

//    public UpdateExecutionFactory getUpdateExecutionFactory() {
//        return updateExecutionFactory;
//    }
//    public QueryExecutionFactory getQueryExecutionFactory() {
//        return queryExecutionFactory;
//    }

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
