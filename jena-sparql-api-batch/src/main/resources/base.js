{
  $prefixes: { // TODO Add initial context
	dcat: 'http://www.w3.org/ns/dcat#',
	qb: 'http://purl.org/linked-data/cube#',
	grddl: 'http://www.w3.org/2003/g/data-view#',
	ma: 'http://www.w3.org/ns/ma-ont#',
	org: 'http://www.w3.org/ns/org#',
	owl: 'http://www.w3.org/2002/07/owl#',
	prov: 'http://www.w3.org/ns/prov#',
	rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
	rdfa: 'http://www.w3.org/ns/rdfa#',
	rdfs: 'http://www.w3.org/2000/01/rdf-schema#',
	rif: 'http://www.w3.org/2007/rif#',
	rr: 'http://www.w3.org/ns/r2rml#',
	sd: 'http://www.w3.org/ns/sparql-service-description#',
	skos: 'http://www.w3.org/2004/02/skos/core#',
	skosxl: 'http://www.w3.org/2008/05/skos-xl#',
	wdr: 'http://www.w3.org/2007/05/powder#',
	'void': 'http://rdfs.org/ns/void#',
	wdrs: 'http://www.w3.org/2007/05/powder-s#',
	xhv: 'http://www.w3.org/1999/xhtml/vocab#',
	xml: 'http://www.w3.org/XML/1998/namespace',
	xsd: 'http://www.w3.org/2001/XMLSchema#',

	gldp: 'http://www.w3.org/ns/people#',
	cnt: 'http://www.w3.org/2008/content#',
	earl: 'http://www.w3.org/ns/earl#',
	ht: 'http://www.w3.org/2006/http#',
	ptr: 'http://www.w3.org/2009/pointers#',

	cc: 'http://creativecommons.org/ns#',
	ctag: 'http://commontag.org/ns#',
	dc: 'http://purl.org/dc/terms/',
	dcterms: 'http://purl.org/dc/terms/',
	dc11: 'http://purl.org/dc/elements/1.1/',
	foaf: 'http://xmlns.com/foaf/0.1/',
	gr: 'http://purl.org/goodrelations/v1#',
	ical: 'http://www.w3.org/2002/12/cal/icaltzd#',
	og: 'http://ogp.me/ns#',
	rev: 'http://purl.org/stuff/rev#',
	sioc: 'http://rdfs.org/sioc/ns#',
	v: 'http://rdf.data-vocabulary.org/#',
	vcard: 'http://www.w3.org/2006/vcard/ns#',
	schema: 'http://schema.org/',

	tmp: 'http://jsa.aksw.org/tmp/'
  },
  $jsonMacros: {
	  $query: '',
      $scope: '',
      $sparqlService: ''
  },
  $namedQueries: {
clearGeoWgs: '?s | Construct { ?s geo:long ?x ; geo:lat ?y }',
clearGeoSparql: '?s | Construct { ?s ogc:geometry [ ogc:asWKT ?w ] }',
clearGeoSparqlLgd: '?s | Construct { ?s geom:geometry [ ogc:asWKT ?w ] }'
locationToLgd: '?s | Insert { ?s tmp:lgd ?o } Where { ?s tmp:location ?l . Bind(nominatim:geocode(?l) As ?x) . Bind(str(json:path(?x, "$[0].osm_type")) As ?oet) . Bind(str(json:path(?x, "$[0].osm_id")) As ?oei) . Bind(uri(concat("http://linkedgeodata.org/triplify/", ?oet, ?oei)) As ?o) }'
	/* TODO What to do with dangling resources - i.e. geometries that are not releated to a feature */

inferLocationFp7: ''
  },
  $queryMacros: {
	  clearGeo: ['clearGeoWgs', 'clearGeoSparql', 'clearGeoLgd'],
	  inferLocation: ['inferLocationFp7']
  },
}
