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
The following table lists the featured extensions for JSON processing within SPARQL.
Note, that while there is streaming support for the CSV format, at present there is no streaming support for JSON arrays.

| Category  | Synopsis    | Description | 
| --------- | ----------- | ------------|
| **Namespaces**          | `json: <http://jsa.aksw.org/fn/json/>`  | Namespace for json (property) function extensions |
| **Datatypes**           | `xsd:json`  | The URI for JSON literals. Hijacks the xsd namespace for convenience. Note, that the datatype can be used as a cast expression in order to parse strings to json literals. |
| **Functions**           | `literal json:path(xsd:json json, string literal queryString)` | Evaluates a JSON path expression on the given json argument. The type of the returned literal depends on the evaluation result. See the mapping table below. |
| **Property Functions**  | `?jsonArray json:unnest ?jsonItem`  | Yields bindings where the variable in the object position is consecutively bound to the items of the json array in subject position. Subject must evaluate to a xsd:json literal.  |
|                         | `?jsonArray json:unnest (?jsonItem ?index)` | Same as above but in addition binds ?index to the zero-based index of each item. |
|                         | `?jsonObject json:bind ()` | Make all top-level key-value pairs of the given json object available as a SPARQL binding. I.e. all keys can be accessed via SPARQL variables. |

#### Json Path Expressions
Please refer to the (https://github.com/json-path/JsonPath)[JsonPath] project GitHub page for path expression examples and the specification.

#### JSON <-> SPARQL datatype mapping
| JSON        | RDF         |
| ----------- | ----------- |
| string      | xsd:string  |
| number      | xsd:double  |
| boolean     | xsd:boolean |
| json object | xsd:json    |
| json array  | xsd:json    |
| null        | (unbound)   |

#### Example
```sql
PREFIX ex: <http://example.org/>
PREFIX wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#>
CONSTRUCT {
  ?s
    a ex:Pub ;
    rdfs:label ?l ;
    wgs:long ?x ;
    wgs:lat ?y ;
    .
}
{
    "[{\"id\": \"pub1\", \"name\": \"MyFavoritePub\",  \"lon\": \"50\", \"lat\": \"10\" }]"^^xsd:json json:unnest ?item .

    BIND("http://example.org/" AS ?ns) . 
    BIND(URI(CONCAT(?ns, ENCODE_FOR_URI(json:path(?item, "$.id")))) AS ?s) . 
    BIND(json:path(?item, "$.name") AS ?l) . 
    BIND(STR(json:path(?item, "$.lon")) AS ?x) . 
    BIND(STR(json:path(?item, "$.lat")) AS ?y) . 
}
```

Output:
```
@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .
@prefix ex: <http://example.org/> .
@prefix wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#> .

ex:pub1  a          ex:Pub ;
        rdfs:label  "MyFavoritePub" ;
        wgs:lat     "10" ;
        wgs:long    "50" .
```

### CSV
The main principle of the CSV extensions is to parse each row of the input data into a corresponding JSON object.
This means, that all JSON extensions can be used for further processing, and no dedicated CSV datatype is necessary.


| Category  | Synopsis    | Description | 
| --------- | ----------- | ------------|
| **Namespaces**          | `csv: <http://jsa.aksw.org/fn/csv/>`  | Namespace for CSV (property) function extensions |
| **Functions**           | `xsd:json csv:parse(string literal, "optionsString")` | Parse CSV data provided as a string literal into a `xsd:json literal` holding the corresponding JSON array. |
| **Property Functions**  | `?csvUrl csv:parse (?rowJsonObj "optionsString")` | Parse the data at the URL in the subject position as a CSV file with the given options. **Streams** each row and binds it as a JSON object to `?rowJsonObj`. |
|                         | `?csvUrl csv:parse ?rowJsonObj` | Equivalent to `?csvUrl csv:parse (?rowJsonObj "excel")` |

#### CSV option strings

```
[base format] [-s] [-d] [-e] [-h]
```

* Base format: One of the predefined (https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html)[CSV Formats] defined by [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/). Defaults to `excel`.
* `-s`: Field separator, default to `'`
* `-d`: Field delimitor, defaults to `,`
* `-e`: Escape charater, defaults to `\`
* `-h`: User first row as headers, defaults to `false`.

Without headers, the row json object's keys are labeled `col0` to `coln`.


### XML
At present there is XPath integration.


### Related projects:
* TARQL
* JARQL
* (https://ci.mines-stetienne.fr/sparql-generate/)[SPARQL Generate]: This project introduces its own SPARQL-based syntax for facilitating data integration.

