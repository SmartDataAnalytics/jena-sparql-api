{
    todoTRim: "BIND('^\\s*(.*)\\s*$' AS ?trimPattern)",

    taskExecutor: {
      type: 'org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor',
      corePoolSize: 8,
      maxPoolSize: 8
    },

    prefixes: { $prefixes: {
        'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
        'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
        'dbo': 'http://dbpedia.org/ontology/',
        'dbr': 'http://dbpedia.org/resource/'
    } },

    // TODO Configure user-agent for http requests
    httpUserAgent: 'enter@email.here',

    dbpedia: { $sparqlService: ['http://dbpedia.org/sparql', 'http://dbpedia.org'] },

    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://aksw.org/people'] },


    job: { $simpleJob: {
        name: 'testJob12',

        steps: [

            { $sparqlPipe: {
              name: 'fetchDataFromDBpedia',
              chunk: 1000,
              source: '#{ dbpedia }',
              target: '#{ target }',
              query: 'CONSTRUCT { ?s ?p ?o } { ?s a dbo:Person ; dbo:birthPlace dbr:Leipzig ; ?p ?o . Filter(?p In (rdf:type, rdfs:label, dbo:birthPlace)) }'
            } },

            { $shell: {
                name: 'say hello',
                command: 'echo hello world'
            } }
        ]
    } } // end of job
}
