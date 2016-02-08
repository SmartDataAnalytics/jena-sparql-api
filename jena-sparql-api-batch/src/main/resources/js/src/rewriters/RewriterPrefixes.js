rewriters.push(function(json) {
  var result = json;
  var e = json.$prefixes;
  if(e) {
    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanPrefixes',
      prefixes: { $json: e }
    };
    //print('YEEHAW: ' + JSON.stringify(result));
  }
  return result;
});
