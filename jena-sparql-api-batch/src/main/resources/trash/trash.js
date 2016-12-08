{
test:

                { $sparqlStep: {
                  name: 'geocodeLocations',
                  chunk: 1000,
                  target: '#{ target }',
                  concept: '?s | { ?s a o:Airport }',
                  shape: { $json: [
                    'rdf:type',
                    'rdfs:label'
                  ] },
                  modifiers: [ 'DELETE WHERE { ?s rdfs:label ?o . }', '...' ]
                } },
foo:

{
    source: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },
    target: { $sparqlService: ['http://localhost:8890/sparql', 'http://fp7-pp.publicdata.eu/'] },

    job: { $simpleJob: {
        name: 'geoCodingJob',
        steps: [

        ]
    } }
},

xxx:{

    { $sparqlUpdate: {
        name: 'clearData',
        target: '#{ target }',
        update: 'DELETE WHERE { ?s ?p ?o}'
    } }

}




}