## Extensions for Jena SPARQL Statement Handling

Jena provides the Query and UpdateRequest domain classes, however it lacks a unifying infrastructure.
The goal of this module is to provide it.


## Features

* Interfaces and implementations for a uniform infrastructure of SPARQL statements and SPARQL parsers
* Removal of unused prefixes
* Utils to apply element / algebra operations on both queries and update requests.

## Design

### Sparql Statements
The main interface is `SparqlStmt` which has the specializations `SparqlQueryStmt`, `SparqlUpdateStmt` and `SparqlStmtUnknown`.
Each SparqlStmt instance is constructed from a String or an Object representation.
A SparqlStmt *always* has a string representation obtainable via `toString`.
A SparqlStmt *may* be parsed whereas parsing of the original string *may* have failed with the cause retrievable via `getParseException`.
If a SparqlStmt was parsed, `toString` will return the SPARQL serialization of the parsed object (Query or UpdateRequest).
In that case, `getOriginalString` yields the string that was fed to the parser.

Retaining the original string is particularly useful is middleware scenarios: Parsing of a statement may fail due to undefined prefixes in the middleware, but forwarding the orginal string to another endpoint may still succeed.


```java
public interface SparqlStmt {
    boolean isQuery();
    boolean isUpdateRequest();
    boolean isUnknown();

    boolean isParsed();

    SparqlStmtUpdate getAsUpdateStmt();
    SparqlStmtQuery getAsQueryStmt();

    QueryParseException getParseException();
    String getOriginalString();

    PrefixMapping getPrefixMapping();

    SparqlStmt clone();

    default Query getQuery();
    UpdateRequest getUpdateRequest();
}
```


### SPARQL Parsers
A SparqlStmt parser is conceptually a `Function<String, SparqlStmt>`.
Implementations of parser support configuration, namely prefix mapping, syntax and base IRI.
Likewise, `SparqlQueryParser` and `SparqlUpdateParser` are `Function<String, SparqlQueryStmt>` and `Function<String, SparqlUpdateStmt>`, respectively.

Jena provides static methods in the `QueryFactory` and `UpdateFactory` classes for parsing SPARQL queries and update requests.
These methods are wrapped `SparqlQueryParserImpl` and `SparqlUpdateParser`.

The `SparqlStmtParserImpl` implementation for convenience provides several static factory methods. Under the hood they create a `SparqlQueryParser` and `SparqlUpdateParser`. Parsing a statement first runs the query parser and if it fails, the update parser is invoked.
Upon creation of parsers, the `actAsClassifier` flag can be set which controls whether to raise exceptions if both parsers fail or whether to yield SparqlStmt instances with `isParser()` returning false.
If `actAsClassifier` is enabled and both parsers fail, the query string is classified depending on which parser consumed the most bytes from the input (based the line / column information of the raised parse exception). On this basis, an appropriate `StmtQueryStmt` or `SparqlUpdateStmt` instance is created. If parsing suceeded, SparqlStmt.isParsed will return true and the `getQuery` or `getUpdateRequest` will return the appropriate object.


```java
SparqlStmtParser parser = SparqlStmtParserImpl.create();

// If you only need to deal with UpdateRequests, use can use SparqlUpdateParserImpl instead:
// SparqlUpdateParser parser = SparqlUpdateParserImpl.create();

SparqlStmt stmt = parser.apply("PRFIX foo: <http://foo.bar/baz/> INSERT DATA { <urn:s> <urn:p> <urn:o> }");
System.out.println("isParsed: " + stmt.isParsed());
System.out.println("UpdateRequest.toString(): " + stmt.getUpdateRequest());

// Remove the unused `foo:` prefix (in place transformation)
SparqlStmtUtils.optimizePrefixes(stmt);

```


#### Enhancing parser functionality

Prefix optimization can be added to any SparqlStmtParser using a wrapper function
```java
SparqlStmtParser parser;
parser = SparqlStmtParser.create();
parser = SparqlStmtParser.wrapWithOptimizePrefixes(parser);
```

Namespace tracking inserts all seen namespaces to a PrefixMapping instance. The PrefixMapping may in turn be
consulted by the parser:


```java
PrefixMapping pm = new PrefixMappingImpl();
pm.setNsPrefixes(PrefixMapping.Extended);

SparqlStmt stmt;
SparqlStmt parser;
parser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, pm, /* actAsClassifier= */ true);
parser = SparqlStmtParser.wrapWithNamespaceTracking(pm, parser);


stmt = parser.parse("SELECT * { ?s a eg:Foobar }");
System.out.println("parsed: " + stmt.isParsed());
// Printed 'false' because eg: is an unknown prefix

parser.parse("PREFIX eg: <http://www.example.org/> SELECT * { }");

parser.parse("SELECT * { ?s a eg:Foobar }");
System.out.println("parsed: " + stmt.isParsed());
// Printed 'true' because eg: is known be a prior statement

```


#### SPARQL Stmt Iterator
Using the machinary, files containing sequences of SPARQL statements can be processed in a similar fashion as `.sql` scripts that are comprised of a sequence of SQL satements.
Given an `InputStream` and a `SparqlStmtParser` the utility function
`SparqlStmtIterator parse(InputStream in, Function<String, SparqlStmt> parser)` bundles the two together into an iterator that reads SPARQL statements from that stream. In order to read from a file or classpath resource the class `SparqlStmtMgr` provides useful convenience methods:


```java
PrefixMapping pm = new PrefixMappingImpl();
List<Query> SparqlStmtMgr.loadQueries("file.sparql", pm);

```


