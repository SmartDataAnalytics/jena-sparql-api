# Conjure - Conjuring data from data

### Dev Status
Development is progressing rapidly:

* ongoing: sansa integration
* 2019 Oct 4: Sophisticated example prototypically working
* 2019 Oct 3: Designed and implemented dataset algebra, assembled working prototype, ported all relevant code from dcat-suite, lots of clean up
* 2019 Oct 1: This is work in process; entity processing is implemented, dataset processing is in the works

[Demo code in works for me state](./src/test/java/org/aksw/jena_sparql_api/conjure/test/MainConjurePlayground.java)

## Introduction

Conjure is an extremely powerful, light-weight, scalable, algebra-based approach to data processing workflow specifications for RDF:
The algebraic approach enables one to specify a blue-print for the construction of a dataset in terms of references to datasets and SPARQL-queries.
This blue print can then be passed to an executor which can interact with the triple store of your choice.
The expression nature of the workflow specification gives you the opportunity of caching intermediate and/or final datasets.
The dataset references give executors the chance to look up available distributions in data catalogs, which in turn allows for discovery and re-use of existing databases as well as automatic setup of local ones.


Specifically for RDF, Conjure features:

* Separation of workflow specification from workflow execution - execute workflows against the triple store of your choice
* Caching of generated artifacts - e.g. a workflow that performs summarization of a dataset, such as computing a [VoID](https://www.w3.org/TR/void/) description, only needs to be executed once. Afterwards, an executor will typically serve the result from cache.


## Algebras

Conjure ships with two related, yet indepedent, algebras and correspoding executors:

Notation notes:
* `{1..n}`: A set of 1 or more items
* `[1..n]`: A list of one or more items

### Entity processing algebra
This is an algebra for byte-level transformations. We refer to byte sources as entities. Hence, the algebra allows transformations of entities to other entities.

* `OpDataset`: Primitive operation. References an entity by id.
* `OpPath`: Primitive operator. References a file on the file system.
* `OpConvert(subOp, sourceContentType, targetContentType)`: Unary operation to convert an entity from one content type to another.
* `OpCode(subOp, coderName, isDecode)`: Unary operation to apply encoding or decoding to an entity.


#### API
Creation of the workflow is pretty straight forward. Note that the Op classes are Jena-Resources backed by an in-memory RDF model.


```java
// Construction of an expression
OpPath a = OpPath.create(Paths.get("/data.nt.gz"));
OpCode b = OpCode.create(a, "gzip", /* decode = */ true);
OpConvert c = OpConvert.create(b, "application/n-triples", "application/rdf+xml");
OpCode d = OpCode.create(c, "bzip2", false);

// Write out the RDF - because we can.
Assert.assertEquals(a.getModel(), d.getModel())
RDFDataMgr.write(System.out, d.getModel(), RDFFormat.TURTLE_PRETTY);

// Polymorphism of the RDF is handled by jena-sparql-api-mapper-proxy,
// so the following assertion works:
Assert.assertTrue(d.getSubOp() instanceof OpConvert);

// The simplest implementation of the executor is as a visitor
OpVisitor<Path> executor = new OpExecutor(repository, hashStore);
Path result = d.accept(executor);
```

Don't forget to check out [jena-sparql-api-mapper-proxy](../jena-sparql-api-mapper-proxy) to learn about how to bridge the RDF and Java worlds using annotations instead of writing code.

### RDF dataset processing algebra
This is an algebra for transforming (triple-based) RDF datasets with SPARQL. The operations produce datasets from the input datasets.

* `OpDataset`: Primitive operation.  References an entity by id.
* `OpPath`: Primitive operator. References a file on the file system.
* `OpConstruct(subOp, constructQuery{1..n})`: Unary operation that constructs a dataset from another one by execution of a set of construct queries
* `OpUnion(subOp{1..n})`: N-ary operation that constructs a dataset from a set of child datasets
* `OpUpdateRequest(subOp, updateRequest[1..n])`: Unary operation to derive a dataset by running a sequence of update requests


## DataObject and Dataset Reference Abstractions

A DataObject represents a means of access to a specific instance of a dataset.
A dataset reference is a specification on which basis to create DataObjects.

### Data References

A dataset reference is a specification on which basis to create DataObjects - i.e. a bunch of attributes.

* DataRefFromUrl: Reference an RDF dataset by a URL. A Semantic Web framework, such as Jena, should try its best to make sense from it.
* DataRefFromSparqlDataset: URL to a SPARQL service + named graphs
* DataRefFromDcat: Reference to a URI that is further described in an entry in a DCAT catalog. I think we can allow references to any of datasets, distributions and download urls.
   The catalog itself may again be described with a DataRef.



**Proposal:** References to Java objects (e.g. Jena Models) can be done via JNDI using DataRefFromUrl with a jndi: URI scheme.
So with JNDI we have a standard way to bridge references to remote data and data within the JVM in a uniform way.


#### Data Reference vs DCAT Distribution
In my view, DCAT distributions are generally more high level than data references, but for simple cases, they may be the same thing:
A data reference is specifically focused on covering the technical aspects of gaining access to a digital copy.
So a dcat distribution with a downloadURL 
can be seen as a DataRefFromUrl (which might be a use case for inferencing).

For this reason, native DCAT support seems reasonable via DataRefFromDcat: A DataRef processor can examine the referenced DCAT
record and possibly rewrite/substitute/replace the DataRefFromDcat with a DataRefFromUrl.


DCAT distributions however may either not refer to a a concrete access mechanism
("on this HTML page is a step by step guide for how to download the data"), or they may specify whole workflows - for example based on conjure or DEER.
The latter part is not standardized, but from my perspective it should become one.


### Data Objects
A DataObject represents a specific digital copy of a dataset, and acts as the provider for means of access to it.
A dataset is defined as an instance of a datamodel, so for a dataset there can be any
 number of digital copies based on any type of storage and access mechanisms.

DataObjects can be closed in order to indicate that no more access to the digital copy of the dataset is needed. 
Life cycle managers for DataObjects can then trigger appropriate actions, such as freeing allocated resources.

At present, there is only the RdfDataObject specialization.
The RdfDataObject allows for obtaining a single RDFConnection at a time in order to query or modify the dataset it represents.

For example, a DataObject may be backed by a SPARQL endpoint or by an in-memory Model.
However, closing the DataObject may overwrite an existing file with the most recent state of the model. Maybe the close operation even does a git commit of the dataset.
As another example, closing a DataObject may trigger shut down of a dockerized triple store - because it was indicated that no more access to the dataset is needed, and therefore
allocated resources can be freed.

So to emphasize the difference between a DataObject and a connection to it:
As long as the DataObject is 'alive' many connections can be openend and closed on it.
The connection is just a tool - a means of access - for dataset access - but it does not represent a dataset itself.

Obviously, especially in the dockerized scenario, resource pooling can go on behind the scenes, such that a triple store in a docker container
may supply multiple data objects.


In any case, the application is decoupled from the concerns of resource deallocation - just make sure you invoke .close() when you are done.









## HTTP Resource Store and Repository
Implementation of a file-system-based HTTP Resource Store:
The store implementation uses the file system to store content and metadata.

The repository is front end allows to retrieve content using URIs and HTTP headers plus content negotiation.
The repository actually manages three HTTP Resource stores:

* The download store of actual downloaded content
* The cache store for content generated for HTTP resources from the download store and shipped as HTTP responses, such as by converting content types (e.g. RDF/XML to turtle) and altering encoding (e.g. gzp instead of bzip2)
* The hash store for intermediate artifacts of workflow executions - this is content that was generated to serve requestes to the repository, but that was not
the final content being shipped to the client.


## Related Work

The approach of this work shares similarity with the [RDF Data Enrichment Framework (DEER)](https://dice-group.github.io/deer/).
The differences are:

* DEER's operator implementations use Jena models which limits scalability.
* Conjure's scope is broader w.r.t. dataset and service management (referenced datasets need to be made accessible using SPARQL endpoints). Conjure's greatest selling point is to decouple references to datasets from the SPARQL services that make them accessible.
* Conjure's scope is narrower w.r.t. operator types, as the aim is to offload the heavy lifting to SPARQL queries that are sent to the service(s).
* What still has to be considered is whether building execution graphs from algebraic specifications brings even further benefits. [Faraday Cage](https://github.com/dice-group/faraday-cage) seems to have gone this way. One obvious feature that could be provided this way would be closure operations on datasets - e.g. "repeat until no more change" in conjuction with SPARQL queries that implement inference rules.



