# Query Containment Module

This module contains interfaces and abstract algorithms for query containment.
"Abstract" means, that these implementations are not bound to a specific API, but instead are parameterized using a combination of generics and lambdas.


## The Query Containment API
The index works by transforming SPARQL queries such that their leaf nodes become conjunctive queries.
Checking for query containments works by creating candidate matchings between these leaf nodes as the basis for performing a bottom-up
traversal of the algebra expression trees.


### Simple Usage
Testing for homomorphic query containment can be done using

```java
boolean isContained = SparqlQueryContainmentUtils.checkContainment("", "");
```

```java
SparqlQueryContainmentIndex<Node, ResidualMatching> index = SparqlQueryContainmentIndex.create();



index.put(viewKey, viewOp);
Stream<Entry<Node, SparqlTreeMapping>> tmp = index.match(userOp)

```

Using the method signatures of the index directly is actually simple, although the low level result types are somewhat unwieldly due to their gener



```java
SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> sii = ExpressionMapper.createIndex(validate);

TriFunction<OpContext, OpContext, Table<Op, Op, BiMap<Node, Node>>, NodeMapperOp> nodeMapperFactory = NodeMapperOpContainment::new;  
QueryContainmentIndex<Node, Graph<Node, Triple>, Var, Op, ResidualMatching> index = QueryContainmentIndexImpl.create(sii, nodeMapperFactory);

index.put(viewKey, viewOp);
Stream<Entry<Node, TreeMapping<Op, Op, BiMap<Var, Var>, ResidualMatching>>> tmp = index.match(userOp)

```



```java
QueryContainmentIndex<K, org.jgrapht.Graph<Node, Triple>, Var, Op, R> index = QueryContainmentIndexSparql.create();


```





## Related Resources
* The query containment benchmark is located in this repository at [/benchmarking/sparqlqc-jena3]
* Our index structure for subgraph isomorphisms is located at [https://github.com/SmartDataAnalytics/SubgraphIsomorphismIndex].


## Graph based containment checks


### Converting algebra expressions to RDF graphs
```
QueryToJenaGraph.
```

### Converting predicate expressions to RDF graphs
```
QueryToJenaGraph.
```



### Abstract Interfaces

```java
public interface QueryContainmentIndex<K, G, N, A, V> {
}
```


```
```


### Mapping Expression


