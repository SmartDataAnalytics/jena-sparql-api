{
    todoTRim: "BIND('^\\s*(.*)\\s*$' AS ?trimPattern)",

    taskExecutor: {
      type: 'org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor',
      corePoolSize: 8,
      maxPoolSize: 8
    },

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
        name: 'testJob5',

        steps: [
//            { $sparqlUpdate: {
//                name: 'clearData',
//                target: '#{ target }',
//                update: 'DELETE WHERE { ?s ?p ?o }'
//            } },
//
//            { $sparqlPipe: {
//              name: 'loadData',
//              chunk: 1000,
//              source: '#{ source }',
//              target: '#{ target }',
//              query: 'CONSTRUCT WHERE { ?s ?p ?o }',
//              filter: 'term:valid(?s) && term:valid(?p) && term:valid(?o)'
//            } },

            { $shell: {
                name: 'say hello',
                command: 'echo hello world'
            } }
        ]
    } } // end of job
}
