{
	/**
	 * Metadata
	 */
    id: 'geoCodingJob',
    jobRepository: 'defaultJobRepo',
    context: {
        ssf: {
            type: 'org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp'
        },
        fp7pp: {
            type: 'org.springframework.beans.factory.config.MethodInvokingFactoryBean',
            targetObject: {ref: 'ssf'},
            targetMethod: 'createSparqlService',
            arguments: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/', null]
        },
    	shape: {
    		$shape: {
			    'fp7o:funding': {
			      'fp7o:partner': {
			        'fp7o:address': {
			          'fp7o:country': 'rdfs:label',
			          'fp7o:city': 'rdfs:label'
			        }
			      }
			    }
    		}
		  },
        update: ' \
INSERT { \
    ?s foo:lgd ?l \
} WHERE \
  VALUES(?s) { (<http://nominatim.openstreetmap.org/search/?format=json&q=Leipzig>) } \
  BIND(http:get(?s) As ?json). \
  ?json json:unnest ?item. \
  BIND(json:path(?item, '$.osm_type') AS ?osmType) \
  BIND(json:path(?item, '$.osm_id') AS ?osmId) \
  BIND(json:path(?item, '$.lon') AS ?x) \
  BIND(json:path(?item, '$.lat') AS ?y) \
  BIND(concat('http://linkedgeodata.org/triplify/', ?osmType, ?osmId) AS ?l) \
}'
    },
    steps: [{
        $sparqlStep: {
        	chunk: 1000,
        	concept: '?s | ?s a <http://fp7-pp.publicdata.eu/ontology/Project>',
        	shape: { ref: 'shape' },
        	source: { ref: 'fp7pp'},
        	target: { ref: 'fp7pp'},
        	modifier: { ref: 'update' }
        }
    }]
}


 /*
	 * processors: ['spatial', {type: 'interlinking', ctor: 'someConfigString'},
	 * 'write_all_spatial' ],
	 *
	 */
      /*
		 * { type: 'createProperty' property: 'http://myProperty' term: { value: {
		 * fn: 'function() { }' args: ['http://] } } }*]
		 *
		 *
		 * steps: [{ context: { },
		 *
		 * tasklet: {
		 *  }, chunk: { size: 1000, reader: null, processor: null, writer: null }
		 *
		 *  }, { // Perform geocoding and write intermediate result type:
		 * 'default', // By default a step is comprised of an (item) reader,
		 * processor and writer reader: { type: "sparql", service: { ref:
		 * srcSparqlService }, concept: '(?s, ?s a Pub)' // fetch the model for
		 * these referenced resources //attributes: [ 'rdfs:label', 'vcard:city] //
		 * null = everything, [] = nothing //queryString: 'Construct { ?s ?p ?o } {
		 * ?s ?p ?o . ?s a ex:Pub }' }, // note: if processor is an array, the
		 * processing is chained processor: [{ // build the address string first
		 * type: "javascript", code: "function foo(a, b, c, d) { return [a, b,
		 * c, d].join(' '); }", fnName: "foo", // can we automatically figure
		 * out all available functions in a script engine? argmap:
		 * ["vcard:city", "vcard:address | node" ], // Simple mapping of
		 * properties to function arguments //argtype: 'simple' // how to
		 * interpret the argmap / i.e. whether or not to convert RDF nodes to
		 * primitve java objects targetProperty: "http://address" }, { type:
		 * "geocoder.nominatim", apiUrl: "http://nominatim.openstreetmap.org",
		 * srcProperty: "http://address", format: "geometry", // select what
		 * information of the geocoder is wanted (we could allow specifying a
		 * javascript function) targetProperty: "http://tmp" }, { type:
		 * "generate-related-resourcesprs", desc: "Create geometries resources",
		 * property: 'geom:geometry', replacement: "geometry" // could be a
		 * function { js: 'function() { } ' } }, { type: "move-values", srcPath:
		 * "http://tmp", tgtPath: "geom:geometry ogc:asWKT" }, { type: "diff" //
		 * should diff creation be a processing step? } ], writer: { type:
		 * "sparql", service: { ref: dstSparqlService} }
		 *
		 * }], xstep: { // Clear geometries of resources for which an
		 * intermediate result exists }, ystep: { // Write the intermediate
		 * result } } } // { ?s a <http://fp7-pp.publicdata.eu/ontology/Project> } /* {
		 * ?s <http://fp7-pp.publicdata.eu/ontology/address> ?o . ?o
		 * <http://fp7-pp.publicdata.eu/ontology/city> ?city . ?o
		 * <http://fp7-pp.publicdata.eu/ontology/country> ?country } //
		 * '$special': ['spatial'],
		 */
  //

  // Note: Looks like we could YAML with this approach:
	// http://stackoverflow.com/questions/23744216/how-do-i-convert-from-yaml-to-json-in-java


// lgdShape: {
// 'geom:geometry': 'ogc:asWKT',
// 'rdfs:label': false
// },

/*
 * prefixes: { type: 'com.hp.hpl.jena.shared.impl.PrefixMappingImpl',
 * $postActions: [ $exec: {} ] },
 */
// $prefixes: { // Special attribute for global prefixes
// },
// prefixes: {
// $prefixes: { // Macro for creating a PrefixMapping object
// fp7o: 'http://fp7-pp.publicdata.eu/ontology/',
// vcard: 'http://www.w3.org/2006/vcard/ns#',
// ex: 'http://example.org'
// }
// },
// nominatimLookupService: {
// type: 'org.aksw.jena_sparql_api.geo.NominatimLookupServiceFactory'
// },
// srcSparqlService: {
// type: 'org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp',
// ctor: ['http://fp7-pp.publicdata.eu/sparql']
// //ctor: ['http://dbpedia.org', ['http://dbpedia.org']]
// //ctor: ['http://dbpedia.org', {type: 'foo.bar.DatasetDescription', ctor:
// ['http://dbpedia.org']}]
// },
// dstSparqlService: {
// type: 'org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp',
// ctor: ['http://linkedgeodata.org']
// },
// baseConcept: '{ ?s a fp7:Partner }'

// baseConcept: { '?s <http://fp7-pp.publicdata.eu/ontology/address> ?o . ?o
// <http://fp7-pp.publicdata.eu/ontology/city> ?city . ?o
// <http://fp7-pp.publicdata.eu/ontology/country> ?country'
