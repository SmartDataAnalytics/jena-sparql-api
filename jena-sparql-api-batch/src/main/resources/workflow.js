{
    todoTRim: "BIND('^\\s*(.*)\\s*$' AS ?trimPattern)",

    prefixes: { $prefixes: {
       'foo': 'http://bar',
       'geo':'http://www.w3.org/2003/01/geo/wgs84_pos#',
       'o': 'http://fp7-pp.publicdata.eu/ontology/'
    } },

    // TODO Configure user-agent for http requests
    httpUserAgent: 'enter@email.here',

    source: { $sparqlService: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/'] },

    //source: { $sparqlService: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/'] },

    // Intermediate stores that also act as caches:
    resloc: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/resloc/'] },
    geocoderCache: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/locjson/'] },


    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },

    job: { $simpleJob: {
        name: 'geoCodingJob',

        steps: [
            { $sparqlUpdate: {
                name: 'clearData',
                target: '#{ target }',
                update: 'DELETE WHERE { ?s ?p ?o }'
            } },

            { $sparqlPipe: {
              name: 'loadData',
              chunk: 1000,
              source: '#{ source }',
              target: '#{ target }',
              query: 'Construct Where { ?s ?p ?o }',
              filter: 'term:valid(?s) && term:valid(?p) && term:valid(?o)'
            } },

            { $sparqlUpdate: {
                name: 'clearLocations',
                target: '#{ target }',
                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . Filter(?p In (geo:lat, geo:long)) }'
            } },

            { $sparqlPipe: {
                name: 'createLocations',
                chunk: 1000,
                source: '#{ target }',
                target: '#{ resloc }',
                query: '\
CONSTRUCT { \
  ?s tmp:location ?l \
} \
WHERE { \
  ?s\
    o:address [\
      o:country [ rdfs:label ?col ] ; \
      o:city [ rdfs:label ?cil ] \
    ] \
  BIND(str(?cil) As ?clis) \
  BIND(concat(?cils, if(strlen(?cils) > 0, " ", ""), ?col) As ?l) \
}'
            } },

            // TODO: A sparql pipe to a different graph in the same store could be optimized by the engine
            { $sparqlPipe: {
                name: 'createLocationStringResources',
                chunk: 1000,
                source: '#{ resloc }',
                target: '#{ geocoderCache }',
                query: 'CONSTRUCT { ?x tmp:hasLocation ?l } WHERE { ?s tmp:location ?l . Bind(uri(concat("http://example.org/location/", encode_for_uri(?l))) As ?x) }'
            } },


            { $sparqlStep: {
                name: 'geocodeLocations',
                chunk: 1,
                service: '#{ geocoderCache }',
                concept: '?l | { ?x tmp:hasLocation ?l . Optional { ?x tmp:geocodeJson ?j } Filter(!Bound(?j)) }',
                hop: { $hop: {
                    queries: [
                        [ '?l | CONSTRUCT WHERE { ?x tmp:hasLocation ?l }', '#{ geocoderCache }' ],
                        [ '?l | CONSTRUCT WHERE { ?s tmp:location ?l }', '#{ resloc }']
                    ]
                } },

//                shape: { $json: {
//                    'tmp:hasLocation': false,
//                    'tmp:geocodeJson': false
//                } },
                modifiers: [
'\
INSERT { \
    ?x tmp:geocodeJson ?j \
} \
WHERE { \
\
  { SELECT ?l (http:get(concat("http://nominatim.openstreetmap.org/search?format=json&email=cstadler%40informatik.uni-leipzig.de&polygon_text=1&q=", http:encode_for_qsa(?l))) AS ?tmpJ) { \
    { SELECT DISTINCT ?l { \
      ?x tmp:hasLocation ?l \
    } } \
  } } \
  ?x tmp:hasLocation ?l . \
  Bind(if(strlen(str(?tmpJ)) > 5000000, "[]", ?tmpJ) As ?j) \
}'
                //'DELETE WHERE { ?x tmp:geocodeJson ?j }'
]
            } }
//
//
//
//            { $sparqlStep: {
//                name: 'createLgdUrls',
//                chunk: 1000,
//                source: '#{ resloc }',
//                concept: '?l | ?s tmp:location ?l',
//                hop: { $hop: {
//                  queries: [
//                    [ '?l | CONSTRUCT { ?x tmp:geocodeJson ?j } WHERE { ?x tmp:geocodeJson ?j ; tmp:hasLocation ?l }', '#{ geocoderCache }'],
//                    [ '?l | CONSTRUCT WHERE { ?x tmp:lgdLink ?l }', '#{ source }']
//                  ]
//                } },
//                modifiers: ['DELETE WHERE { ?s tmp:lgdLink ?l }',
//'\
//INSERT { \
//    ?s tmp:lgdLink ?l \
//} WHERE { \
//  ?s tmp:geocodeJson ?j \
//\
//  BIND(json:path(?item, "$[0].osm_type") AS ?osmType) \
//  BIND(json:path(?item, "$[0].osm_id") AS ?osmId) \
//  BIND(concat("http://linkedgeodata.org/triplify/", ?osmType, ?osmId) AS ?l) \
//}'],
//                target: '#{ target }'
//            } },
//
//            { $sparqlUpdate: {
//                name: 'clearWgs84',
//                target: '#{ target }',
//                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . Filter(?p In (geo:lat, geo:long)) }'
//            } },
//
////            { $sparqlUpdate: {
////                name: 'enrichWithLgd',
////                target: '#{ target }',
////                update:
////            } },
//
//            { $sparqlUpdate: {
//                name: 'tmpToSameAs',
//                target: '#{ target }',
//                update: 'INSERT { ?s owl:sameAs ?l } WHERE { ?s tmp:lgdLink ?l }'
//            } },
//
//            { $sparqlUpdate: {
//                name: 'removeTmp',
//                target: '#{ target }',
//                update: 'DELETE { ?s tmp:lgdLink ?l } WHERE { ?s tmp:lgdLink ?l }'
//            } }
//
//// Jena does not support MODIFY???
////            { $sparqlUpdate: {
////                name: 'renameLgdLinks',
////                target: '#{ target }',
////                update: 'MODIFY DELETE { ?s tmp:lgdLink ?l } INSERT { ?s owl:sameAs ?l } WHERE { ?s tmp:lgdLink ?l }'
////            } },
//
////            { $sparqlUpdate : {
////                name: 'fuseSameAs',
////                target: '#{ target }',
////                update: 'MODIFY DELETE { ?o ?x ?y } INSERT { ?s ?x ?y } WHERE { ?s owl:sameAs ?o . ?o ?x ?y}'
////            } }
        ]
    } } // end of job
}
