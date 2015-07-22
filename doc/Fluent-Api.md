## Fluent API

The jena-sparql-api offers several fluent classes for conveniently creating and configuring instances of QueryExecutionFactory, UpdateExecutionFactory, SparqlService and SparqlServiceFactory.
These fluent classes have the same names as those just mentioned, prefixed with `Fluent` - such as `FluentQueryExecutionFactory` or `FluentQueryExecutionFactoryFn`.
There are also two versions of each fluent: Those whose name ends in `Fn` and the ones that do not.

* The 'Fn' versions, which we refer to as _function fluents_, are use to build _decoration functions_ that can be lazily applied to entities (e.g. QueryExecutionFactory).
* The non-'Fn' versions are used to apply decorations _directly_ to a specific entity.

The jena-sparql-api fluents generally offer these 3 methods:
* .end() Move up to the parent fluent in case of nested fluents. If no such parent exists, an exception is raised indicating that a call to .create() is expected.
* .value() Returns the value built so far with the fluent. This method should not be used directly.
* .create() Returns the result of .value() iff this method is called on the root fluent, i.e. the one which does not have a parent. Otherwise throw an exception that indicates that a call to .end() is expected.

Function fluents in addition offer the method:
* .compose(Function<T, T> nextFn) Append a new decoration function to the prior ones. This is an extension point for adding custom decorators that are not natively supported by the FluentAPIs.


### Examples

Building a query execution factory with pagination and delay between requests:
```java
QueryExecutionFactory qef = FluentQueryExecutionFactory
    .http("http://dbpedia.org/sparql", "http://dpbedia.org")
    .config()
        .withDelay(500, TimeUnit.MILLISECONDS)
        .withPagination(1000)
    .end()
    .create();
```

Same as above, however with explicitly creating the decoraton function:
```java
// Create the decorator function
Function<QueryExecutionFactory, QueryExecutionFactory> decoratorFn =
    QueryExecutionFactoryFn.start()
        .withDelay(500, TimeUnit.MILLISECONDS)
        .withPagination(1000)
        .create();

// At some point create a QueryExecutionFactory ...
QueryExecutionFactory qef = FluentQueryExecutionFactory
    .http("http://dbpedia.org/sparql", "http://dpbedia.org")
    .create();

// ... and apply the decorations
qef = fn.apply(qef);
```

Example of how to build a SparqlService
```java
SparqlService sparqlService = FluentSparqlService
    .http("http://dbpedia.org/sparql", "http://dpbedia.org")
    .config() // Enter configuration via FluentSparqlServiceFn
        .configQuery() // Enter configuration of the query aspects via FluentQueryExecutionFactoryFn
            .withPagination(100)
        .end()
    .end()
    .create();
```

### Further Reads
[SparqlUpdate](SparqlUpdate)



