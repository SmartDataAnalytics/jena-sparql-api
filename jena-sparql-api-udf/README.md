## User defined function (UDF) Module

This module provides useful extensions to Jena's built-in function extension system:

Primarily, it provides an RDF-based domain model to define function macros in RDF together with an importer that registers the macros at jena's functions registry.

A key feature is the support to put function definitions into profiles because vendor support for certain SPARQL function may vary.

The central class is [UserDefinedFunctionResource](src/main/java/org/aksw/jena_sparql_api/user_defined_function/UserDefinedFunctionResource.java).


The [service description module](../jena-sparql-api-service-description) contains resources to probe features of SPARQL endpoints - including the vendor - such that profiles can be activated automatically for many conventional triple stores.


The application of this module happens in the [algebra](../jena-sparql-api-algebra) module.


### Blank Node to IRI Rewriting

The most prominent application of this module is the blank node rewriting middleware that can transform blank nodes in a remote endpoint into URIs (using a special bnode:// uri scheme) and *vice versa*.
This works by rewriting SPARQL queries and exploiting vendor-specific functions that yield a blank node's internal label.


The macro definitions are in the algebra module's resource [bnode-rewrites.ttl](../jena-sparql-api-algebra/src/main/resources/bnode-rewrites.ttl).



