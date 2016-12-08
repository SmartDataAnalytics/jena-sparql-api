### Setup
Release versions are published on the
[central maven repository](http://search.maven.org/#search|ga|1|a%3A%22jena-sparql-api-cache%22)


Development snapshots are available in
[our aksw archiva](http://maven.aksw.org/archiva/#artifact/org.aksw.jena-sparql-api/jena-sparql-api-cache)


Use this snipped to add the AKSW maven repository:
```xml
    <repositories>
        <repository>
            <id>maven.aksw.snapshots</id>
            <name>AKSW Snapshot Repository</name>
            <url>http://maven.aksw.org/archiva/repository/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>
```

The artifact; please pick the latest version for the above repositories.
```xml
            <dependency>
                <groupId>org.aksw.jena-sparql-api</groupId>
                <artifactId>jena-sparql-api-cache</artifactId>
                <version>...</version>
            </dependency>
```

### Background

The Jena framework distinguishe between a query's syntatic and algebraic representation and its execution, and provides extension points for each of these components.
The cache abuses/exploits the SPARQL SERVICE keyword for marking parts of a query subject to caching but for which no cache entry was found:

Example: The following query when procecessed by the cache system ...
```sql
SELECT  * {
  ?s <http://ex.org/p1> ?o1
}
```

... is rewritten to:
```sql
SELECT  * {
  SERVICE <cache://ex.org/cacheid-322561962> {
    ?s <http://ex.org/p1> ?o1
  }
}
```

When the query is executed, a custom executor detects the cache:// URL in the service keyword and thus attempts to
execute the contained graph pattern against the remote SPARQL service. If the result set's size is below a configurable threshold,
it is associated with that graph pattern and added to the cache.

For subsequent queries that make use of the same graph pattern (regardless of variable naming), the cache system injects the priorly cached result set.
So the next time the first query is fired, it will be rewritten to

```sql
SELECT  * {
  VALUES(?s ?o) { ... }
}
```

The current version scans a query for projected filtered quad patterns (PFQPs); these are
expression of the form Project(Vars, Filter(Exr, QuadPattern(...)).
PFQPs are subject to caching.
Every PFQP is then surrounded by a service keywords, whereas the service URL uses the cache:// URI scheme.
(In the future it makes sense to include distinct).


Jena uses a global registry for factories that create algebra executors.

### Usage

```java

// First, our custom OpExecutionFactory needs to be registered, which performs cache actions of graph patterns
// under SERVICE clauses that make use of the `cache://` URI scheme.
OpExecutionFactoryCache opExecutionFactory = new OpExecutionFactoryCache();
QC.setFactory(ARQ.getContext(), opExecutionFactory);


// Create a qef for the local or remote base SPARQL service
Model model = RDFDataMgr.loadModel(fileName);
QueryExecutionFactory baseQef = FluentQueryExecutionFactory
    .from(model)
    // Use http for remote access
    //.http("http://akswnc3.informatik.uni-leipzig.de/data/dbpedia/sparql", "http://dbpedia.org")
    //.http("http://localhost:8890/sparql", "http://dbpedia.org")
    .config()
        .withParser(SparqlQueryParserImpl.create(Syntax.syntaxARQ))
        .withQueryTransform(F_QueryTransformDatesetDescription.fn)
        .withPagination(1000)
    .end()
    .create();


// Now wrap the base qef with the caching features
// Note, that the qef that adds caching needs access to the executors service map in order
// to temporarily register sub-qefs for executing the graph patterns under cache:// SERVICE references.
QueryExecutionFactory qef = new QueryExecutionFactoryViewCacheMaster(baseQef, opExecutionFactory.getServiceMap());

QueryExecution qe = qef.createQueryExecution("PREFIX o: <http://ex.org/ont/> SELECT * { ?s a o:Person }");
ResultSet rs = qe.execSelect();
String str = ResultSetFormatter.asText(rs);
System.out.printn(str);
```


#### Example with SparqlServiceFactory
```java



SparqlServiceFactory ssf = new SparqlServiceFactory() {
    @Override
    public SparqlService createSparqlService(String serviceUri,
            DatasetDescription datasetDescription, Object authenticator) {

        SparqlService coreSparqlService = FluentSparqlService
            .http(serviceUri, datasetDescription, (HttpAuthenticator)authenticator)
                // Perform additional configuration via the fluent API
            .create();
        
        
        SparqlService r = ... // Add custom decorators not covered by the fluent
        return r;
    }
};

ssf = FluentSparqlServiceFactory.from(ssf)
        .configFactory()
            .defaultServiceUri("http://localhost:8890/sparql") // If createSparqlService is called with a null argument, this URI is passed on instead
            .configService()
                .configQuery()
                    .withPagination(1000) // Add pagination, delay, to your liking
                .end()
            .end()
        .end()
        .create();

SparqlService ss = ssf.createSparqlService(null, null, null); // Obtain a SPARQL service factory (making use of the defaultServiceUri).
```


