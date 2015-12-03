{
    prefixes: { $prefixes: {
       'jsa': 'http://ns.aksw.org/jassa/ontology/'
    } },

    source: { $sparqlService: ['#{jobParameters["sourceUrl"]'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://sparql.cc/'] },

    job: { $simpleJob: {
        name: 'analysisJob',

        steps: [
            { $sparqlPipe: {
              name: 'loadData',
              chunk: 1000,
              source: '#{ source }',
              target: '#{ target }',
              query: 'CONSTRUCT { ?x o:joinsWith ?y } { SELECT DISTINCT ?x ?y { ?a ?x [ ?y ?b ] }',
            } },
        ]
    } }
}
