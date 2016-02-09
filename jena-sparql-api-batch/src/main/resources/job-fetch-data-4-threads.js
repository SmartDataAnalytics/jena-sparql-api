{
    prefixes: { $prefixes: {
       'lgdo': 'http://linkedgeodata.org/ontology/'
    } },

    source: { $sparqlService: ['http://linkedgeodata.org/sparql', 'http://linkedgeodata.org'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://linkedgeodata.org'] },

//    test: {
//        type: 'java.lang.String',
//        scope: 'step',
//        ctor: ['foo']
//    },

    taskExecutor: {
      type: 'org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor',
      corePoolSize: 8,
      maxPoolSize: 8
    },

    fetchQuery: 'CONSTRUCT { ?s ?p ?o } { ?s a lgdo:City ; ?p ?o }',

    job: { $simpleJob: {
        name: 'fetch-data-4-threads',

        steps: [
            { $sparqlCount: {
                name: 'countStep',
                target: '#{ target }',
                query: '#{ fetchQuery }',
                key: 'fetchQueryCount'
            } },

            { $log: {
                name: 'logStep',
                text: '## jobExecutionContext[fetchQueryCount]'
            } },

            { $log: {
                name: 'logStep2',
                text: 'yay'
            } },

            { $sparqlPipe: {
                name: 'pipe',
                chunk: 1000,
                taskExecutor: '#{ taskExecutor }',
                throttle: 4,
                source: '#{ source }',
                target: '#{ target }',
                query: '#{ fetchQuery }'
            } }
        ]
    } }
}
