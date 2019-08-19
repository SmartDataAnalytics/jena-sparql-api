## Reactive RDF / SPARQL Stream Processing

This module provides reactive wrappers for reading RDF files and executing SPARQL queries based on RxJava.
In a nutshell, reactive streams are similar yet more powerful than Java's standard `java.util.Stream`s because of their support
for "back-pressure" and cancellation of processes. In RxJava's terminology, reactive streams are realized using *Flowables'.


### RDFDataMgrRx
Jena's `RDFDataMgr` is the central class for loading RDF data in various formats from various sources (files, URLS, classpath).
`RDFDataMgr` provides reactive wrappers for Jena's `RDFDataMgr`.

The reactive versions allow e.g. limiting the number of triples / quads to read from the input.

The core functions from reading triple and quad based formats are the following:
```java
Flowable<Quad> createFlowableQuads(Callable<InputStream> inSupplier, Lang lang, String baseIRI);
Flowable<Triple> createFlowableTriples(Callable<InputStream> inSupplier, Lang lang, String baseIRI); 
```
The signatures of these methods are similar to Jena's RDFDataMgr `createIteratorTriples` and `createIteratorQuads` methods.
The main difference is, that the first argument of the reactive version is a **supplier** of an `InputStream` rather than an `InputStream` directly. As long as no terminal operation is invoked on the flowables, no input stream is requested from the supplier. Cancelling the flowable also closes any obtained input stream.

In addition, further methods based on the core methods are provided.



#### Triple-based flowables
Reading of triples (with limit and timeout) is demonstrated below: 

```
Graph graph = GraphFactory.createDefaultGraph();

Flowable<Triple> tripleFlow = createFlowableTriples(() -> new FileInputStream("my-file.ttl"), Lang.TURTLE, "http://www.example.org/")
  .limit(100)
  .timeout(10, TimeUnit.SECONDS)
  .forEach(graph::add);


```

#### Quad Processing
This is akin to triple-based processing, except that the method `createFlowableQuads` is to be used.


#### Dataset Flowables
Datasets are sets of quads.
One simple pattern for realizing streams of RDF graphs is to group them by graph names.
This allows any **consecutive sequence** of triples **with the same graph** to be returned as a Dataset.

In this example (in trig notation), there are two graphs :g1 and :g2
```
:g1 {
  :s1 :p :o .
  :o :x :y
}

:g2 {
  :s2 :p :o .
  :o :x :y
}
```

```java
long countDatasets = Flowable<Dataset> datasetFlow = RDFDataMgrRx.createFlowableDatasets(
	() -> new FileInputStream("my-trig-file.trig"), Lang.TRIG, "http://www.example.org/")
	.count().blockingGet()

System.out.println(countDatasets);
// Prints '2'
```


A special case is, where the graph name matches a resource in its contained graph. The is e.g. useful for RDF stream processing of statistical observations:

```
:observation1 {
  :observation1 :p :o .
  :o :x :y
}
```

In this case, we provide support for reading such a stream of quads as a stream of Jena Resources. For each graph, a Jena Model with all corresponding triples is created and a resource with the name of the graph is returned. Hence, all triples are accessible via the resource's model - even if the graph resource has no directly related triples.

```
Flowable<Resource> resourceFlow = createFlowableResources(() -> new FileInputStream("my-trig-file.trig"), Lang.TRIG, "http://www.example.org/");

```


##### Writing Dataset streams
A flow of resources can be converted in graph-wrapped stream of quads using
```java
void writeResources(Flowable<? extends Resource> flowable, Path file, RDFFormat format);
```
  
**NOTE: Multiple consecutive resources from the flow are serialized alternately with a 'distinguished://' prefix in order to prevent data conflation**.
The `readResources` method appropriately handles these cases on deserialization.


```java
void writeDatasets(Flowable<? extends Dataset> flowable, Path file, RDFFormat format);
```

Note, at present there is no mechanism for cancelling the write process.


### SparqlRx
The main advantace of the reactive SPARQL utils is to easily facilitate sophisticated SPARQL response processing using a stream API, which in addition allows for cancellation at any time.


The core functions only depend on the supplier of a Jena Query Execution object:
```
Flowable<QuerySolution> execSelect(Supplier<QueryExecution> qes);
Flowable<Binding> execSelectRaw(Supplier<QueryExecution> qes);
```

Hence, these methods can be easily used with different query execution generation approaches:

```
String queryString = "SELECT * { ?s ?p ?o } LIMIT 10";

Flowable<QuerySolution> qsFlow1 =  SparqlRx.execSelect(
    () -> QueryExecutionFactory.create(queryString, model));

RDFConnection conn = RDFConnectionFactory.connect("http://dbpedia.org", sparql);

Flowable<QuerySolution> qsFlow2 = SparqlRx.execSelect(
    () -> conn.query(queryString));
```


#### RDFNode / Resource-centric Query Processing
In several cases, resource-centric processing bears advantages
over processing of bindings and query solutions:

* Typically, one wants to process objects rather than bindings. Jena supports creating a domain-specific view over any RDF resource using `Resource.as(View.class)` - provided, that the view has been registered.
* Attributes can be accessed using standard vocabularies rather than ad-hoc variable names.
* Serialization of resources is essentially an RDF graph or dataset and can be done using the util methods of RDFDataMgrRx.

The basic idea of resource-centric processing is to take a SPARQL CONSTRUCT query and select one its nodes (typically variables) as the *root*.

```java
Flowable<RDFNode> execPartitioned(SparqlQueryConnection conn, Node s, Query q);
```

The flow of `RDFNode`s can now be easiliy mapped to e.g. Resource or a custom resource view:
```java
Flowable<RDFNode> nodeFlow = SparqlRx.execPartitioned(conn, Var.alloc("s"), QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { ?s a :Person }"));

Flowable<Resource> resourceFlow = nodeFlow.map(RDFNode::asResource);
Flowable<Person> personFlow = nodeFlow.map(n -> n.as(Person.class));
```

Putting it together yields:
```java
List<Person> people = SparqlRx.execPartitioned(conn, var, query)
    .map(r -> r.as(Person.class))
    .toList().timeout(10, TimeUnit.SECONDS).blockingGet());
```

This approach is **extremely powerful** when used in conjunction with annotation-driven resource view generation using the
[jena-sparql-api-mapper-proxy](../jena-sparql-api-mapper-proxy) module.

It is valid to create a flow from a CONSTRUCT query with an empty template, such as `CONSTRUCT { } { ?s a :Person }`. In this case the result is still a flow of RDFNode / Resource objects based on the partition variable. However, these nodes are than backed by an empty model.




