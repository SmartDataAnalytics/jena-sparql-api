## SPARQL extensions for heterogeneous data

This module enhances Jena's SPARQL engine with additional datatypes, functions and property functions for accessing and processing JSON, CSV and XML data.

Simply by including the following maven dependency will automatically make all features available. This works because the JAR file
provides appropriate metadata which gets picked up by Jena's plugin system.

```xml
<dependency>
    <artifactId>org.aksw.jena-sparql-api</artifactId>
    <groupId>jena-sparql-api-sparql-ext</groupId>
    <version><!-- Check link below for latest version--></version>
</dependency>
```
Click (http://search.maven.org/#search|ga|1|a%3A%22jena-sparql-api-sparql-ext%22)[here] to check for the latest version of the dependency


## Data formats

### JSON

| Category  | Synopsis    | Description | 
| --------- | ----------- | ------------|
| **Namespaces**          | `json: <http://jsa.aksw.org/fn/json/>`  | Namespace for json (property) function extensions |
| **Datatypes**           | `xsd:json`  | The URI for JSON literals. Hijacks the xsd namespace for convenience  |
| **Functions**           | `xsd:json json:parse(string literal str)`  | Parse the argument `str` as a literal of datatype xsd:json. Yields type error if parsing fails. |
|                         | literal json:path(xsd:json json, string literal queryString) | Evaluates a JSON path expression on the given json argument. The type of the returned literal depends on the evaluation result. See the mapping table below. |
| **Property Functions**  | ?jsonArray json:unnest ?jsonItem  | Yields bindings where the variable in the object position is consecutively bound to the items of the json array in subject position. Subject must evaluate to a xsd:json literal.  |
|                         | ?jsonArray json:unnest (?jsonItem ?index) | Same as above but in addition binds ?index to the zero-based index of each item. |


#### JSON <-> SPARQL datatype mapping
| JSON        | RDF         |
| ----------- | ----------- |
| string      | xsd:string  |
| number      | xsd:double  |
| boolean     | xsd:boolean |
| json object | xsd:json    |
| json array  | xsd:json    |
| null        | (unbound)   |

#### Examples



Related projects:
* TARQL
* JARQL
* (https://ci.mines-stetienne.fr/sparql-generate/)[SPARQL Generate]: This project introduces its own SPARQL-based syntax for facilitating data integration.

