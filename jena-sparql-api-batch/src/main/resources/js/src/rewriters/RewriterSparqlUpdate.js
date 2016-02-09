rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlUpdate;
  if(e) {
    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepTasklet',
      name: e.name,
      tasklet: {
        type: 'org.aksw.jena_sparql_api.batch.step.TaskletSparqlUpdate',
        scope: 'step',
        target: e.target,
        update: e.update
      }
    };

  }
  return result;
});
