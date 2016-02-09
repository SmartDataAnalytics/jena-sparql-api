rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlUpdate;
  if(e) {
    result = {
      type: 'org.springframework.batch.core.step.tasklet.TaskletStep',
      scope: 'step',
      name: e.name,
      tasklet: {
        type: 'org.aksw.jena_sparql_api.batch.step.TaskletSparqlUpdate',
        target: e.target,
        update: e.update
      }
    };

  }
  return result;
});
