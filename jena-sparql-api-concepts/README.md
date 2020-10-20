## Concept Module

This module contains:

* SPARQL Relation Pattern API: Enables vastly simplified construction and combination of SPARQL graph patterns. Supports constructing JOIN and FILTER operations, and most notably takes care of correct variable renaming.
  * The Main classes are: `Relation` (and subclasses UnaryRelation, BinaryRelation, TernaryRelation)
  * A *Concept* is a pair of a graph pattern and a variable thereof - so its essentially a sparql query with exactly 1 result columns
* Concept Path Finder: Finds simple paths of RDF properties between a given start and end concepts.

A note on naming: For brevity we refer to relation patterns simply as relations as this is what they describe.

```java
// Create the set ?s, where ?s is a person:
UnaryRelation filter = Concept.parse("?s | ?s a eg:Person");

// Create a SPARQL fragment that for any resource ?s yields all its rdf:types to ?t
// - with the addition, that for resources without a type, ?t will be set to the IRI 'eg:unbound'
UnaryRelation pattern = Concept.parse("?t | OPTIONAL { ?x a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:unbound) AS ?t)", PrefixMapping.Extended);


// Create a new SPARQL fragment:
UnaryRelation result = pattern
    // Create a combined graph pattern by prepending that of 'filter' to that of 'pattern'
    // Note, that prepend leaves the variables of the calling fragment (i.e. 'pattern') intact, whereas for 'filter',
    // all occurrences of ?s will be substituted with ?x
    .prependOn(Var.alloc("x")).with(filter)
    // The prior statement yields a generic Fragment - make it unary again
    .toUnaryRelation();

//              v Prepended and renamed element
// Result: ?t | ?x a eg:Person . OPTIONAL { ?x a ?tmp } BIND(IF(BOUND(?tmp), ?tmp, eg:unbound) AS ?t)

```


Example Usage of the Concept Path Finder Java API:

See the test cases for latest working code [TestConceptPathFinder.java](jena-sparql-api-concepts/src/test/java/org/aksw/jena_sparql_api/sparql_path/core/TestConceptPathFinder.java)

```java
// Load some test data and create a sparql connection to it
Dataset ds = RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl");
RDFConnection dataConnection = RDFConnectionFactory.connect(ds);

// Set up a path finding system
ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();

// Use the system to compute a data summary
// Note, that the summary could be loaded from any place, such as a file used for caching
Model dataSummary = system.computeDataSummary(dataConnection).blockingGet();

// Build a path finder; for this, first obtain a factory from the system
// set its attributes and eventually build the path finder.
ConceptPathFinder pathFinder = system.newPathFinderBuilder()
	.setDataSummary(dataSummary)
	.setDataConnection(dataConnection)
	.build();

// Create search for paths between two given sparql concepts
PathSearch<Path> pathSearch = pathFinder.createSearch(
	Concept.parse("?s | ?s eg:cd ?o", PrefixMapping.Extended),
	Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended));

// Set parameters on the search, such as max path length and the max number of results
// Invocation of .exec() executes the search and yields the flow of results
List<Path> actual = pathSearch
	.setMaxLength(10)
	.setMaxResults(10)
	.exec()
	.toList().blockingGet();

```

