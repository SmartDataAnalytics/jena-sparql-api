
## Background Caching Tooling
The purpose is to stream a query's response data simultaneously to the cache and to the client.
Thereby, different slices of the same query may be requested simultaneously.


In order to retrieve a sub-sequence of the items of a query response, a wrapping with `LazyLoadingCacheList` which provides a `retrieve(Range<Lang>)` method is used.
```java
public class LazyLoadingCachingList<T> {
    ClosableIterator<T> retrieve(Range<Long> range);
}
```







## Concepts and Components

### Terminology
Algebra expression and algebra tree are used interchangebly.

### Sparql Algebra Extensions



* BINDING-STORE-GET(<storeId>, filterExpr): Retrieve solutions bindings from the store with ID storeId using the provided filter expression
* BINDING-STORE-PUT(<storeId>, executionQuery, indexAlgebraExpression): Execute the executionQuery and associate the result set with indexAlgebraExpression. The service on which to execute executionQuery is taken from this query execution's execution context.

Implementation exploits the SERVICE keyword, to represent the GET and PUT operations.

Store operations are:

* putResultSet(<storeId, resultSet)
* getResultSet(<storeId>)

### Sparql Algebra Tagger
Substitutes (sub-)expression of a given expression with references to GET or PUT operations.


### View Matcher Lookups


### Full query lookups
Purpose: Check if the same query, except for slice and variable naming, is either already running or has been cached.
So for the slice based caching, we need a complete match.

```java
class OpLookup {
void add(Op);

Collection<Entry<Op, OpVarMap>> lookup(Op op)
}
```


### SparqlCacheSystem
```java
QueryExecutionFactoryViewCacheMaster
QueryExecutionViewCacheFragment
```

```java
class OpExecutorViewCache {
    protected Map<Node, ViewCacheIndexer> serviceToQef;
}
```


```java
public interface ViewCacheIndexer {

    //@Override
    QueryExecution createQueryExecution(Op indexPattern, Query query);

}
```

### TODOs




### QueryIndex
The query index structure (maybe rename to summary) summarizes relevant features of a query.

```java
public class QueryIndex {
    /**
     * Index over all of a query's quad patterns
     * Allows retrieving any of a query's quad patterns using a given set of features
     * 
     */
    protected FeatureMap<Expr, QuadPatternIndex> quadPatternIndex;
}
```


### QuadPatternIndex
```java
public class QuadPatternIndex {
    /**
     * Index of an individual DNF clauses (i.e. this is not an index over the whole DNF)
     * Each key of the map corresponds to a blocking key, whereas an entry's set of values
     * is a subset of this clause's conjunction according to the blocking key
     */
    protected Multimap<Expr, Expr> groupedConjunction;
    
    /**
     * Reference to the node in the algebra expression
     */
    protected TreeNode<Op> opRef;
        
    /**
     * The opRef's corresponding qfpc
     */
    protected QuadFilterPatternCanonical qfpc;
}
```

### IndexSystem (candidate selection)
```java
/**
 * A datastructure which allows putting data of a type C into it,
 * and enables querying candidates with type Q.
 */
public interface IndexSystem<C, Q> {
    void add(C item);
    Collection<C> lookup(Q query);
}
```

For our system, we want to use algebra expression and lookup _candidate_ items that _may_ be sub-trees of the originial query.
The computation of tree / variable mappings is then to be carried out at a subsequent phase.

IndexSystem<Entry<Op, QueryIndex>, Op> indexSystem;
Function<Op, QueryIndex> queryIndexer;

### 








