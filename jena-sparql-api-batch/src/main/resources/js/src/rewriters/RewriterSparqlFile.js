rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlFile;

  if(e) {
    if(_.isObject(e)) {
      throw new Error('Argument not supported: ' + e);
    } else { // if e is primitive - see https://github.com/lodash/lodash/issues/1406
      result = {};
      result['type'] = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanSparqlFile';
      result['fileNameOrUrl'] = e;
    }
  }
  return result;
});
