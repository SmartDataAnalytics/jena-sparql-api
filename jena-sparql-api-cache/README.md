
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








