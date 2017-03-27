rewriters.push(function(json) {
  var result = json;
  var e = json.$sparqlCount;
  if(e) {
//	    result = {
//	              type: 'org.springframework.batch.core.step.tasklet.TaskletStep',
//	              ctor: [e.name],
//	              tasklet: {
//	                type: 'org.aksw.jena_sparql_api.batch.tasklet.TaskletSparqlCountData',
//	                ctor: [e.query, e.target, e.key]
//	              }

    result = {
      type: 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepTasklet',
      name: e.name,
      tasklet: {
        type: 'org.aksw.jena_sparql_api.batch.tasklet.TaskletSparqlCountData',
        scope: 'step',
        ctor: [e.query, e.target, e.key]
      }
    }
  }
  return result;
});
