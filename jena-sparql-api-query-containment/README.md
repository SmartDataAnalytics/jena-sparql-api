# Query Containment Module

This module contains interfaces and abstract and concrete algorithms for query containment (QC).
"Abstract" means, that these implementations are not bound to a specific API, but instead are parameterized using a combination of generics and lambdas. Presently, there is one concrete parametrization for SPARQL based on the [Apache Jena](https://jena.apache.org/) framework.

## Artifact
```xml
<dependency>
    <groupId>org.aksw.jena-sparql-api</groupId>
    <artifactId>jena-sparql-api-query-containment</artifactId>
    <version><!-- Check link below --></version>
</dependency>
```
Check here for the [latest version](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.aksw.jena-sparql-api%22%20a%3A%22jena-sparql-api-query-containment%22).


## Usage
A simple boolean QC check can be performed using:
```java
Query vStr = "SELECT * { ?a ?b ?c }";
Query qStr = "SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) }";
boolean isContained = SparqlQueryContainmentUtils.tryMatch(vStr, qStr);
```

QC Testing for homomorphic query containment can be done using:


```java
Query v = QueryFactory.create(vStr, Syntax.syntaxSPARQL_10);
Query q = QueryFactory.create(qStr, Syntax.syntaxSPARQL_10);

Op vOp = Algebra.compile(v);
Op qOp = Algebra.compile(q);

SparqlQueryContainmentIndex<String, ResidualMatching> index = SparqlQueryContainmentIndexImpl.create();
index.put("v", vOp);

// The result is a stream of entries, where the key is the matching index entry,
// and value carries information about how the normalized algebra expression tree of that entry can be aligned to one of q.
Stream<Entry<String, SparqlTreeMapping<ResidualMatching>>> candidates = index.match(qOp);

```

Complete simple examples can be found at these locations:

* [A simple example test case](src/test/java/org/aksw/jena_sparql_api/query_containment/core/TestSparqlQueryContainmentSimple.java)
* [Test cases for the Inrialpes QC benchmark](src/test/java/org/aksw/jena_sparql_api/query_containment/core/TestSparqlQueryContainmentWithInrialpesQcBenchmark.java)


## Advanced Usage

There exist further create() methods on [SparqlQueryContainmentIndexImpl](src/main/java/org/aksw/jena_sparql_api/query_containment/index/SparqlQueryContainmentIndexImpl.java), of which the most generic one takes two arguments:

* The [Subgraph Isomorphism Index](https://github.com/SmartDataAnalytics/SubgraphIsomorphismIndex) to use for indexing conjunctive queries.
  * This repository provides a simple interface for a Subgraph Isomorphism index together with a JGraphT-based implementation.
  * Additional index implementations with different performance characteristics can thus be easily used for QC testing.
* The factory for creating "NodeMapper" instances: A NodeMapper takes as input
  * a pair of nodes of the AETs of v and q
  * the so far computed containment mapping (i.e. how variables of v are mapped to those of q)
  * a map, which associates every prior pair-wise mapping of the children with an object holding information about the mapping.
  There exist two implementations:
  * [NodeMapperOpEquality](src/main/java/org/aksw/jena_sparql_api/query_containment/index/NodeMapperOpEquality.java) only checks whether for a given pair of AET nodes, the mapped subtrees so far are equivalent
  * [NodeMapperOpContainment](src/main/java/org/aksw/jena_sparql_api/query_containment/index/NodeMapperOpContainment.java) performs additional work. Most prominently, it computes residual filter expressions, such that two AETs `v := FILTER(x, e1 && ... && en)` and q := FILTER(y, f1 && ... && fm)` match, if q's filter expressions are more restrictive than v - or conversely: if q could be derived from v by introducing additional filters to v. 

```java
class SparqlQueryContainmentIndexImpl {
    public static <K, R> SparqlQueryContainmentIndex<K, R> create(
        SubgraphIsomorphismIndex<Entry<K, Long>, Graph<Node, Triple>, Node> index,
        TriFunction<
            ? super OpContext,
            ? super OpContext,
            ? super Table<Op, Op, BiMap<Node, Node>>,
            ? extends NodeMapper<Op, Op, BiMap<Var, Var>, BiMap<Var, Var>, R>> nodeMapperFactory) {
        ...
    }

}
```

## Benchmarking
We set up a [separate project for QC benchmarking](/benchmarking/sparqlqc-jena3) based on [Inrialpe's QC Benchmark v1.4](http://sparql-qc-bench.inrialpes.fr/download.html). Please refer to this site for further information.

Note, that you need to manually fix the download URL as it points to v1.2.
We fixed existing QC checkers to make them reentrant and - because of conflicting versions of their dependencies - bundled them as OSGI plugins such that multiple QC checkers can still be used from the same JVM process by having each of them loaded with a separate class loader.


## License
The source code of this repo is published under the [Apache License Version 2.0](/LICENSE).



