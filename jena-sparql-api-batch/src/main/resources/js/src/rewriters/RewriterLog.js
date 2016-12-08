rewriters.push(function(json) {
  var result = json;
  var e = json.$logStep;

  if(e) {
    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepTasklet',
      name: e.name,
      tasklet: {
        type: 'org.aksw.jena_sparql_api.batch.tasklet.TaskletLog',
        scope: 'step',
        text: e.text
      }
    };
  }
  return result;
});
