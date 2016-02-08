rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlCount;
  if(e) {
    result = _({}).extend(e);
    result.type = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlCount';
  }
  return result;
});
