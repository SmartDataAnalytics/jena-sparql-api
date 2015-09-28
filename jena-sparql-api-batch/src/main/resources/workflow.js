{
    ssf: {
        type: 'org.aksw.jena_sparql_api.core.SparqlServiceFactoryHttp'
    },
    fp7pp: {
        type: 'org.springframework.beans.factory.config.MethodInvokingFactoryBean',
        targetObject: {ref: 'ssf'},
        targetMethod: 'createSparqlService',
        arguments: ['http://fp7-pp.publicdata.eu/sparql', 'http://fp7-pp.publicdata.eu/', null]
    },
    target: {
        type: 'org.springframework.beans.factory.config.MethodInvokingFactoryBean',
        targetObject: {ref: 'ssf'},
        targetMethod: 'createSparqlService',
        arguments: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/', null]
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
            $sparqlStep: {
                name: 'step1',
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
        }]
    } }
}
