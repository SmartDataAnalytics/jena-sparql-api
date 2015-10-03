{
    fp7pp: { $sparqlService: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },

    sourceFile: { $sparqlFile: 'dbpedia-airport-eu-snippet.nt' },

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
myName: 'foobar',

    job: { $simpleJob: {
        //type: 'org.springframework.batch.core.job.SimpleJob',
        /**
         * Metadata
         */
        name: 'geoCodingJob',
        //jobRepository: 'defaultJobRepo',

        steps: [
//            {
//                $sparqlLoad: {
//                    name: 'load task',
//                    source: '#{ sourceFile }',
//                    target: '#{ target }',
//                    chunk : 1
//                }
//            }, {
//
//            }, {
//                $sparqlPipe: {
//                    source: { ref: 'fp7pp' },
//                    target: '#{ target }',
//                    query: 'Construct { ?s ?p ?o } { ?s ?p ?o . ?s a fp7o:Project }'
//                }
//            },
// simplified syntax: [target, source, query] (query defaults to CONSTRUCT WHERE { ?s ?p ?o }
            { $sparqlUpdate: {
                name: 'updateStep',
                target: '#{ target }',
                update: 'DELETE { ?s ?p ?o } WHERE { ?s ?p ?o}'
            } },

            { $sparqlPipe: {
              name: 'loadStep',
              chunk: 1000,
              source: '#{ fp7pp }',
              target: '#{ target }',
              query: 'Construct Where { ?s ?p ?o }',
              filter: 'term:valid(?s) && term:valid(?p) && term:valid(?o)'
            } }

//            $sparqlUpdate: {
//                target:
//                query: 'Update'
//            }


//            $sparqlStep: {
//                name: ' #{ myName + (1 + 1) }  ',
//                chunk: 1,
//                concept: '?s | ?s a <http://fp7-pp.publicdata.eu/ontology/Project>',
//                shape: { ref: 'shape' },
//                source: { ref: 'target'},
//                target: { ref: 'target'},
//                modifiers: ['DELETE { ?s ?p ?o } WHERE { ?s ?p ?o }']
//            }
//        }
//        {
//            $sparqlStep: {
//                name: 'step2',
//                chunk: 1,
//                concept: '?s | ?s a <http://fp7-pp.publicdata.eu/ontology/Project>',
//                shape: { ref: 'shape' },
//                source: { ref: 'fp7pp'},
//                target: { ref: 'target'},
//                //modifiers: []
//                modifiers: [
//                    { ref: 'update' }
//                ]
//            }
//        }
        ]
    } }
}

/*
---
fp7pp:
  $sparqlService:
    - "http://localhost:8890/sparql"
    - "http://fp7-pp.publicdata.eu/"
target:
  $sparqlService:
    - "http://localhost:8890/sparql"
    - "http://fp7-pp.publicdata.eu/"
 *
 */