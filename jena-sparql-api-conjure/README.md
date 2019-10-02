# Conjure - Conjuring data from data

**This is work in process; entity processing is implemented, dataset processing is in the works ~ 1st Oct 2019**

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


## Related Work

The approach of this work shares similarity with the [RDF Data Enrichment Framework (DEER)](https://dice-group.github.io/deer/).
The differences are:

* DEER's operator implementations use Jena models which limits scalability.
* Conjure's scope is broader w.r.t. dataset and service management (referenced datasets need to be made accessible using SPARQL endpoints). Conjure's greatest selling point is to decouple references to datasets from the SPARQL services that make them accessible.
* Conjure's scope is narrower w.r.t. operator types, as the aim is to offload the heavy lifting to SPARQL queries that are sent to the service(s).
* What still has to be considered is whether building execution graphs from algebraic specifications brings even further benefits. [Faraday Cage](https://github.com/dice-group/faraday-cage) seems to have gone this way. One obvious feature that could be provided this way would be closure operations on datasets - e.g. "repeat until no more change" in conjuction with SPARQL queries that implement inference rules.



