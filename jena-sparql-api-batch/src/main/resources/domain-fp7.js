/**
 * Extra queries for certain domains
 */
{
	$prefixes: {
		fp7o: 'http://fp7-pp.publicdata.eu/ontology/'
	},
    $namedQueries: {
    	fp7Location:
'Insert  {\
  ?s ex:locationString ?l\
}\
Where { \
  ?s o:address [ \
    o:country [ rdfs:label ?country] ; \
    o:city [ rdfs:label ?city ] \
  ] . \
  Bind(concat(?city, ', ', ?country) As ?l) \
}',
    }

}