rewriters.push(function(json) {
  var result = json;
  var e = json.$json;
  if(e) {
    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanJsonElement',
      jsonStr: JSON.stringify(e)
    };
  }
  return result;
});
