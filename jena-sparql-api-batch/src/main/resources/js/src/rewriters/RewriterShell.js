rewriters.push(function(json) {
  var result = json;
  var e = json.$shell;
  if(e) {

    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepTasklet',
      name: e.name,
      tasklet: {
        type: 'org.aksw.jena_sparql_api.batch.tasklet.TaskletExecuteShellCommand',
        scope: 'step',
        ctor: [e.command]
      }
    }
  }
  return result;
});
