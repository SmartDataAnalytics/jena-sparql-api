{
    fp7pp: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },
    $prefixes: {
    },

    shape: { $json: {} },
//    shape: {
//        $json: {
//            'fp7o:funding': {
//              'fp7o:partner': {
//                'fp7o:address': {
//                  'fp7o:country': 'rdfs:label',
//                  'fp7o:city': 'rdfs:label'
//                }
//              }
//            }
//        }
//      },
    update: " \
INSERT { \
    ?s fp7o:lgd ?l \
} WHERE { \
  VALUES(?s) { (<http://nominatim.openstreetmap.org/search/?format=json&q=Leipzig>) } \
  BIND(http:get(?s) As ?json). \
  ?json json:unnest ?item. \
  BIND(json:path(?item, '$.osm_type') AS ?osmType) \
  BIND(json:path(?item, '$.osm_id') AS ?osmId) \
  BIND(json:path(?item, '$.lon') AS ?x) \
  BIND(json:path(?item, '$.lat') AS ?y) \
  BIND(concat('http://linkedgeodata.org/triplify/', ?osmType, ?osmId) AS ?l) \
}",

    job: { $simpleJob: {
        //type: 'org.springframework.batch.core.job.SimpleJob',
        /**
         * Metadata
         */
        name: 'geoCodingJob',
        //jobRepository: 'defaultJobRepo',

        steps: [{
                $sparqlPipe: {
                    source: { ref: 'fp7pp' },
                    target: '#{ target}',
                    query: 'Construct { ?s ?p ?o } { ?s ?p ?o . ?s a fp7o:Project }'
                }
            },{

            $sparqlStep: {
                name: 'step1',
                chunk: 1,
                concept: '?s | ?s a <http://fp7-pp.publicdata.eu/ontology/Project>',
                shape: { ref: 'shape' },
                source: { ref: 'target'},
                target: { ref: 'target'},
                modifiers: ['DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }']
            }
        },
        {
            $sparqlStep: {
                name: 'step2',
                chunk: 1,
                concept: '?s | ?s a <http://fp7-pp.publicdata.eu/ontology/Project>',
                shape: { ref: 'shape' },
                source: { ref: 'fp7pp'},
                target: { ref: 'target'},
                //modifiers: []
                modifiers: [
                    { ref: 'update' }
                ]
            }
        }
        ]
    } }
}
