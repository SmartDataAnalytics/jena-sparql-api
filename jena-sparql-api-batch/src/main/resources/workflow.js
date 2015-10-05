{
//    source: { $sparqlService: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/'] },
    source: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },

    job: { $simpleJob: {
        name: 'geoCodingJob',

        steps: [
            { $sparqlUpdate: {
                name: 'clearData',
                target: '#{ target }',
                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o}'
            } },

            { $sparqlPipe: {
              name: 'loadData',
              chunk: 1000,
              source: '#{ source }',
              target: '#{ target }',
              query: 'Construct Where { ?s ?p ?o } Limit 1000',
              filter: 'term:valid(?s) && term:valid(?p) && term:valid(?o)'
            } },

            { $sparqlUpdate: {
                name: 'clearWgs84',
                target: '#{ target }',
                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . Filter(?p In (geo:lat, geo:long)) }'
            } },

//            { $sparqlUpdate: {
//                name: 'clearLocations',
//                target: '#{ target }',
//                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . Filter(?p In (geo:lat, geo:long)) }'
//            } },

            { $sparqlUpdate: {
                name: 'createLocations',
                target: '#{ target }',
                update: '\
INSERT { \
  ?s tmp:location ?l \
} \
WHERE { \
  ?s\
    o:address [\
      o:country [ rdfs:label ?col ] ; \
      o:city [ rdfs:label ?cil ] \
    ] \
  BIND(concat(?cil, " ", ?col) As ?l) \
}'
            } },

            { $sparqlStep: {
                name: 'geocodeLocations',
                chunk: 1000,
                source: '#{ source }',
                concept: '?s | { ?s tmp:location ?j . Optional { ?s tmp:geocodeJson ?j } Filter(!Bound(?j)) }',
                shape: { $json: {
                    'tmp:location': false,
                    'tmp:geocodeJson': false
                } },
                target: '#{ target }',
                modifiers: [ 'DELETE WHERE { ?s tmp:geocodeJson ?o } ',
'\
INSERT { \
    ?s tmp:geocodeJson ?j \
} \
WHERE { \
\
  { SELECT ?l (http:get(concat("http://nominatim.openstreetmap.org/search?format=json&email=cstadler%40informatik.uni-leipzig.de&polygon_text=1&q=", ?l)) AS ?j) { \
    { SELECT DISTINCT ?l { \
      ?s tmp:location ?l \
    } } \
  } } \
  ?s tmp:location ?l \
}']
            } },

            { $sparqlStep: {
                name: 'createLgdUrls',
                chunk: 1000,
                source: '#{ source }',
                shape: { $json: {
                    'tmp:geocodeJson': false
                } },
                target: '#{ target }',
                modifiers: ['\
INSERT { \
    ?s tmp:lgdLink ?l \
} WHERE { \
  ?s tmp:geocodeJson ?j \
  BIND(json:path(?item, "$[0].osm_type") AS ?osmType) \
  BIND(json:path(?item, "$[0].osm_id") AS ?osmId) \
  BIND(concat("http://linkedgeodata.org/triplify/", ?osmType, ?osmId) AS ?l) \
}']
            } },

//            { $sparqlUpdate: {
//                name: 'enrichWithLgd',
//                target: '#{ target }',
//                update:
//            } },

            { $sparqlUpdate: {
                name: 'tmpToSameAs',
                target: '#{ target }',
                update: 'INSERT { ?s owl:sameAs ?l } WHERE { ?s tmp:lgdLink ?l }'
            } },

            { $sparqlUpdate: {
                name: 'removeTmp',
                target: '#{ target }',
                update: 'DELETE { ?s tmp:lgdLink ?l } WHERE { ?s tmp:lgdLink ?l }'
            } }

// Jena does not support MODIFY???
//            { $sparqlUpdate: {
//                name: 'renameLgdLinks',
//                target: '#{ target }',
//                update: 'MODIFY DELETE { ?s tmp:lgdLink ?l } INSERT { ?s owl:sameAs ?l } WHERE { ?s tmp:lgdLink ?l }'
//            } },

//            { $sparqlUpdate : {
//                name: 'fuseSameAs',
//                target: '#{ target }',
//                update: 'MODIFY DELETE { ?o ?x ?y } INSERT { ?s ?x ?y } WHERE { ?s owl:sameAs ?o . ?o ?x ?y}'
//            } }
        ]
    } } // end of job
}
