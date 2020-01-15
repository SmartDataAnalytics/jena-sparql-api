## Resources for creating service descriptions
Most if not all service descriptions can be assembled using a set of SPARQL queries that either probe for the database management system (DBMS) backing a SPARQL endpoint or test for the individual features in question.

### Probing SPARQL Endpoint

Check [this sparql script](src/main/resources/probe-endpoint-dbms.sparql) for currently supported DBMS

* Command line invocation with [SPARQL integrate](https://github.com/SmartDataAnalytics/Sparqlintegrate)

```bash
REMOTE=http://dbpedia.org/sparql sparql-integrate src/main/resources/probe-endpoint-dbms.sparql spo.sparql
```

**Example Output**:
```turtle
_:Be...9 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/sparql-service-description#ServiceDescription> .
_:Be...9 <http://www.example.org/dbmsShortName> "virtuoso" .
_:Be...9 <http://www.example.org/dbmsVersion> "07.20.3232" .
_:Be...9 <http://www.example.org/dbmsName> "OpenLink-Virtuoso-VDB-Server" .
_:Be...9 <http://www.example.org/dbmsId> "OpenLink-Virtuoso-VDB-Server:07.20.3232" .

```

* Programmatic probing

```java
package org.example;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

import com.google.common.collect.Iterables;

public class ProbeSparqlEndpointDbms {
	public static void main(String[] args) {
		String endpointUrl = "http://dbpedia.org/sparql";

		Map<String, String> env = Collections.singletonMap("REMOTE", endpointUrl);

		// Create a model that will hold the report
		Model report = ModelFactory.createDefaultModel();

		// Run the sparql script against the specified remote enppoint
		RDFDataMgrEx.execSparql(report, "probe-endpoint-dbms.sparql", env::get);

		Property dbmsShortNameProperty = ResourceFactory.createProperty("http://www.example.org/dbmsShortName");
		List<String> dbmsShortNames = report.listObjectsOfProperty(dbmsShortNameProperty)
				.mapWith(n -> n.isLiteral() ? Objects.toString(n.asLiteral().getValue()) : null).toList();
		String firstDbmsShortName = Iterables.getFirst(dbmsShortNames, null);

		System.out.println("Probing result: " + firstDbmsShortName)
	}
}
```


### Related Work

[SPARQL 1.2 Community Group on GitHub](https://github.com/w3c/sparql-12/wiki/Inventory-of-existing-extensions-to-SPARQL-1.1)
