//return (function(rewriters) {

rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlPipe;
  if(e) {
    var result = {};
    _(result).extend(e);
    result['type'] = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlPipe';
  }
  return result;
});

//});
