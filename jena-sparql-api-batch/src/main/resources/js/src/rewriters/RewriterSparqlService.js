rewriters.push(function(json) {
  var result = json;

  var e = json.$sparqlService;
  if(e) {
    if(!_(e).isArray()) {
        throw new Error('Array expected, got: ' + e);
    }

    result = {};
    result.type = 'org.aksw.jena_sparql_api.batch.step.FactoryBeanSparqlService';
    var ks = ['service', 'dataset', 'auth'];

    var zipped = _(ks).zip(e);
    zipped.forEach(function(entry) {
      var k = entry[0];
      var v = entry[1];
      if(v != null) {
        result[k] = v;
      }
    });
  }
  return result;
});