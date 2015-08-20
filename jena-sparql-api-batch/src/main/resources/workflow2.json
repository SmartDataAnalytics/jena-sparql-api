{
  // Note: Looks like we could YAML with this approach: http://stackoverflow.com/questions/23744216/how-do-i-convert-from-yaml-to-json-in-java

  job: {
    id: 'geoCodingJob',
    jobRepository: 'defaultJobRepo',
    context: {
      ssf: {
        type: 'org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp'
      },
      srcSparqlService: {
        type: 'org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp',
        ctor: ['http://dbpedia.org']
        //ctor: ['http://dbpedia.org', ['http://dbpedia.org']]
        //ctor: ['http://dbpedia.org', {type: 'foo.bar.DatasetDescription', ctor: ['http://dbpedia.org']}]
      },
      dstSparqlService: {
        type: 'org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp',
        ctor: ['http://linkedgeodata.org']
      },
      fooBean: {
        type: 'org.springframework.beans.factory.config.MethodInvokingFactoryBean',
        targetObject: {ref: 'ssf'},
        // TODO: Should we allow "targetObject: 'ssf'" - i.e. without the ref
        targetMethod: 'createSparqlService',
        arguments: ['http://dbpedia.org', null, null]
      }
    },
    steps: [{ // Perform geocoding and write intermediate result
        type: 'default', // By default a step is comprised of an (item) reader, processor and writer
        reader: {
          type: "sparql",
          service: { ref: srcSparqlService },
          concept: '(?s, ?s a Pub)' // fetch the model for these referenced resources
          //attributes: [ 'rdfs:label', 'vcard:city] // null = everything, [] = nothing
          //queryString: 'Construct { ?s ?p ?o } { ?s ?p ?o . ?s a ex:Pub }'
        },
           // note: if processor is an array, the processing is chained
        processor: [{ // build the address string first
          type: "javascript",
          code: "function foo(a, b, c, d) { return [a, b, c, d].join(' '); }",
          fnName: "foo", // can we automatically figure out all available functions in a script engine?
          argmap: ["vcard:city", "vcard:address | node" ], // Simple mapping of properties to function arguments
          //argtype: 'simple' // how to interpret the argmap / i.e. whether or not to convert RDF nodes to primitve java objects
          targetProperty: "http://address"
        }, {
          type: "geocoder.nominatim",
          apiUrl: "http://nominatim.openstreetmap.org",
          srcProperty: "http://address",
          format: "geometry", // select what information of the geocoder is wanted (we could allow specifying a javascript function)
          targetProperty: "http://tmp"
        }, {
            type: "generate-related-resourcesprs",
            desc: "Create geometries resources",
            property: 'geom:geometry',
            replacement: "geometry" // could be a function { js: 'function() { } ' }
        }, {
            type: "move-values",
            srcPath: "http://tmp",
            tgtPath: "geom:geometry ogc:asWKT"
        }, {
           type: "diff" // should diff creation be a processing step?
        }
        ],
        writer: {
          type: "sparql",
          service: { ref: dstSparqlService} 
        }
        
    }],
    xstep: { // Clear geometries of resources for which an intermediate result exists
    },
    ystep: { // Write the intermediate result
    }
  }
}