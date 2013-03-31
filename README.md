## Welcome to the Jena SPARQL API project: A collection of simple utilities for Jena offering many goodies!
This library offers several [Jena](http://jena.apache.org/)-compatible ways to *transparently* add delays, caching, pagination and even query transformations before sending off your original SPARQL query.

### Maven

    <repositories>
        <repository>
            <id>maven.aksw.internal</id>
            <name>University Leipzig, AKSW Maven2 Repository</name>
            <url>http://maven.aksw.org/archiva/repository/internal</url>
        </repository>

        <repository>
            <id>maven.aksw.snapshots</id>
            <name>University Leipzig, AKSW Maven2 Repository</name>
            <url>http://maven.aksw.org/archiva/repository/snapshots</url>
        </repository>
    </repositories>


    <dependency>
        <groupId>org.aksw.jena-sparql-api</groupId>
        <artifactId>jena-sparql-api-core</artifactId>
        <version>0.6.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>org.aksw.jena-sparql-api</groupId>
        <artifactId>jena-sparql-api-server</artifactId>
        <version>0.6.0-SNAPSHOT</version>
    </dependency>

### Project structure

This library as of now is composed of two modules:
* `jena-sparql-api-core`: Contains the core interfaces and basic implementations.
* `jena-sparql-api-server`: An abstract SPARQL enpdoint class that allows you to easily create your own SPARQL endpoint. For example, the SPARQL-SQL rewriter [Sparqlify](http://github.com/AKSW/Sparqlify) is implemented against these interfaces.
* `jena-sparql-api-utils`: Utilities common to all packages.
* `jena-sparql-api-example-proxy`: An example how to create a simple SPARQL proxy. You can easily adapt it to add pagination, caching and delays.

### Usage

Here is a brief summary of what you can do. A complete example is avaible [here](https://github.com/AKSW/jena-sparql-api/blob/master/jena-sparql-api-core/src/main/java/org/aksw/jena_sparql_api/example/Example.java).

Http Query Execution Factory

    QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");

Adding a 2000 millisecond delay in order to be nice to the backend

    qef = new QueryExecutionFactoryDelay(qef, 2000);

Set up a cache

    // Some boilerplace code which may get simpler soon
    long timeToLive = 24l * 60l * 60l * 1000l; 
    CacheCoreEx cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
    CacheEx cacheFrontend = new CacheExImpl(cacheBackend);

    qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);

Add pagination with (for the sake of demonstration) 900 entries per page (we could have used 1000 as well).
Note: Should the pagination abort, such as because you ran out of memory and need to adjust your settings, you can resume from cache!

    qef = new QueryExecutionFactoryPaginated(qef, 900);

Create and run a query on this fully buffed QueryExecutionFactory
		
    QueryExecution qe = qef.createQueryExecution("Select ?s { ?s a <http://dbpedia.org/ontology/City> } Limit 5000");
		
    ResultSet rs = qe.execSelect();
    System.out.println(ResultSetFormatter.asText(rs));


### Proxy Server Example
This example demonstrates how you can create your own SPARQL web service.
You only have to subclass `SparqlEndpointBase` and override the `createQueryExecution` method.
Look at the [Source Code](https://github.com/AKSW/jena-sparql-api/blob/master/jena-sparql-api-example-proxy/src/main/java/org/aksw/jena_sparql_api/example/proxy/SparqlEndpointProxy.java) to see how easy it is.

Running the example:

    cd jena-sparql-api-example-proxy
    mvn jetty:run
    # This will now start the proxy on part 5522

In your browser or a terminal visit:

[http://localhost:5522/sparql?service-uri=http://dbpedia.org/sparql&query=Select * { ?s ?p ?o } Limit 10](http://localhost:5522/sparql?service-uri=http%3A%2F%2Fdbpedia.org%2Fsparql&query=Select%20%2A%20%7B%20%3Fs%20%3Fp%20%3Fo%20%7D%20Limit%2010)


## License
The source code of this repo is published under the [Apache License Version 2.0](https://github.com/AKSW/jena-sparql-api/blob/master/LICENSE).

This project makes use of several dependencies: When in doubt, please cross-check with the respective projects:
* [Jena](https://jena.apache.org/)
* [Atmosphere](https://github.com/Atmosphere/atmosphere)
* [Guava](http://code.google.com/p/guava-libraries/)
* [commons-lang](http://commons.apache.org/proper/commons-lang/)
* [rdf-json-writer](https://github.com/kasabi/rdf-json-writer) (currently copied but also under Apache 2.0 license, will be changed to maven dep)



