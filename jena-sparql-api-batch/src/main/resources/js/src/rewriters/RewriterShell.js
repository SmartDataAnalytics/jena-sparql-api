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
        //ctor: _(e.command).isArray() ? e.command : ['/bin/sh', '-c', e.command]
        ctor: e.command
//        ctor: [{
//            beanClassName: 'java.util.ArrayList',
//            type: 'java.lang.String',
//            ctor: _(e.command).isArray() ? e.command : ['/bin/sh', '-c', e.command]
//        }]
      }
    }
  }
  return result;
});
