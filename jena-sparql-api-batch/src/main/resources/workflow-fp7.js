{
	imports: 'domain-fp7',
	beans: {
		src: { $sparqlService: ['http://dbpedia.org/sparql', 'http://dbpedia.org'] },
		tgt: { $sparqlService: ['http://dbpedia.org/sparql', 'http://dbpedia.org'] },
		concept: { $concept: '?s | Filter(?s = <http://fp7-pp.publicdata.eu/resource/project/257943> || ?s = <http://fp7-pp.publicdata.eu/resource/project/256975>)' },

	},
	job: {
		steps: [{

		}]
	}
}