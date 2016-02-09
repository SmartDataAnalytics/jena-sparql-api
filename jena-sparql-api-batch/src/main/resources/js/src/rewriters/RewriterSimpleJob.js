//rewriters.push(function(json) {
//  var result = json;
//  var e = json.$simpleJob;
//  if(e) {
//    result = _{
//      type: 'org.springframework.batch.core.job.SimpleJob',
//      steps: e.steps
//    }
//  }
//  return result;
//});