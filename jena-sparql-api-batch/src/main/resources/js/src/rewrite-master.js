/**
 * The following objects are assumed to be provided:
 * - logger: org.slf4j.Logger
 *
 */

var rewriters = [];


var createSimpleClassRewriter = function(jsonKey, className, scope)
{
  return function(json) {
    var result = json;
    var e = json[jsonKey];

    if(e) {
      result = _.extend({}, e);
      result.type = className;
      if(scope) {
        result.scope = scope;
      }

    }
    return result;
  };
};

//rewriters.push(createSimpleClassRewriter('$log', 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepLog'));

rewriters.push(createSimpleClassRewriter('$dataSource', 'org.springframework.jdbc.datasource.DriverManagerDataSource'));
rewriters.push(createSimpleClassRewriter('$simpleJob', 'org.aksw.jena_sparql_api.batch.step.FactoryBeanSimpleJob'));
//rewriters.push(createSimpleClassRewriter('$sparqlCount', 'org.aksw.jena_sparql_api.batch.step.FactoryBeanStepSparqlCount', 'step'));



var traverse = function(json, rewriter, depth) {
  depth = depth == null ? 0 : depth;
  var result;

  var tmp = json != null ? rewriter(json, depth) : null;
  //if()
  var copy;
  if(_(tmp).isPlainObject()) {
      var isChange = false;
      copy = {};
      _(tmp).forEach(function(oldV, k) {
         var newV = traverse(oldV, rewriter, depth + 1);
         copy[k] = newV;
         if(oldV != newV) {
             isChange = true;
         }
      });
  } else if(_(tmp).isArray()) {
      var isChange = false;
      copy = [];
      _(tmp).forEach(function(oldV, k) {
         var newV = traverse(oldV, rewriter, depth + 1);
         copy.push(newV);
         if(oldV != newV) {
             isChange = true;
         }
      });
      result = isChange ? copy : tmp;
  } else {
      result = tmp == json ? json : tmp;
  }

  result = isChange ? copy : tmp;
  return result;
}


var rewrite = function(json, rewriters) {
    // Successively apply each rewriter to the json document
    var result = _(rewriters).reduce(function(currentJson, rewriter) {
        //print('REWRITE: ' + JSON.stringify(json) + ' mooo ' + rewriters);
        var r = traverse(currentJson, rewriter);
        return r;
    }, json);

    return result;
}


/**
 * Rewriters is expected to be an array of functions that take a JSON
 * object and return a (possibly transformed) one
 */
var rewriteUntilNoChange = function(json, rewriters, maxIterations) {
    maxIterations = maxIterations || 100;

    var result = json;

    for(var i = 0; i < maxIterations; ++i) {
        logger.trace('Entering rewrite iteration #' + i);
        var next = rewrite(result, rewriters);


        if(result == next) {
        //if(_(result).isEqual(next)) {
            break;
        }

        result = next;
    }

    logger.trace('Final json:\n' + result);

    if(i >= maxIterations) {
        throw new Error('Max iterations of rewriting json reached (' + i + ') - endless loop?');
    }

    return result;
}

var pullOutScopedInnerBeans = function(json) {
  var result = {};
  var nextId = 0;
  _(json).forEach(function(v, k) {
      var newV = traverse(v, function(item, depth) {
        var r = item;
        if(item.scope && depth > 1) {
            var id = "scopedBean" + ++nextId;
            result[id] = item;
            r = '#{ ' + id + ' }';
        }
        return r;
      });
      result[k] = newV;

  });
  return result;
}



/**
 * The entry point
 *
 */
var performRewrite = function(jsonStr) {
    var json = JSON.parse(jsonStr);
    var finalJson = rewriteUntilNoChange(json, rewriters);

    //finalJson = pullOutScopedInnerBeans(finalJson);

    var result = JSON.stringify(finalJson);
    return result;
}

