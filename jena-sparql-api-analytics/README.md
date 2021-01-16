## jena-sparql-api-analytics

A dedicated package for analytics such that their dependencies can be separated from the rest of the jena-sparql-api project.


### PrefixAccumulator

Depends on PatriciaTrie from org.apache.commons:commons-collections4.

Accumulate prefixes up to a capped size.
Particularly useful for query optimization over virtual knowledge graphs: The prefix metadata can be used for source selection in query federation engines.

* Adding a prefix that is shorter than an existing one will "supersede" (remove) the longer one: So if "ab" exists, "a" is added then only "a" remains.
* When accumulating an item that exceeds the threshold the set of prefixes having the longest
common prefix (lcp) are removed and the lcp is added.

> :warning: At present this operation performs in O(n) whereas only the children of the deepest lca of the patricia trie's leafs would have to be collapsed. TODO: Investigate whether this can be done with the PatriciaTrie API.


#### Example
The following example uses the core implementation of accumulator directly:

```java
List<String> items = Arrays.asList("dbr:Leipzig", "dbr:London", "dbr:City");

PrefixAccumulator acc = new PrefixAccumulator(3);
items.stream().forEach(acc::accumulate);


System.out.println(acc.getValue());
// [dbr:City, dbr:Leipzig, dbr:London]

acc.accumulate("lgd:foo");
System.out.println(acc.getValue());
// [dbr:City, dbr:L, lgd:foo]

```

It is possible to wrap the accumulator as a Java8 Collector suitable for parallel procsessing.
Note, that in the case of parallel processing the result will be correct but may be non-deterministic as the output of the
combine step depends on the set of items the accumulators have seen w.r.t. the given threshold.

```java
Set<String> result = Stream.of("dbr:Leipzig", "dbr:London", "dbr:City", "lgd:foo")
                           .collect(Aggregators.createCollector(() -> new PrefixAccumulator(3)));
System.out.println(result);
// [dbr:City, dbr:L, lgd:foo]

```

Several static convenince methods to work with Jena objects are provided, such as

```java
public static Map<Var, Set<String>> analyzePrefixes(ResultSet rs, int targetSize) { }
```


