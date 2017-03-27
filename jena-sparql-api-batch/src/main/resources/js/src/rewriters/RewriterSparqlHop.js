
//var expectArrayOrNull = function(json) {

//    var result;
//    if(json == null) {
//        result = []
//    } else if(_(json).isJsonArray()) {
//        result =
//    } else {
//        throw new Error("Array expected, instead got: " + json);
//    }
//
//    return result;
//};

var processHops = function(json) {

  // Wrap a non-array as an array containing it as an item
  var hops = _(json).isArray() ? json : [json];
  var result = hops.map(function(hop) {
    var r = processHop(hop);
    return r;
  });
  return result;
};

/**

 * A hop must be a json object with the optional attributes
 * - queries
 * - relations
 *
 * @param json
 * @return
 */
var processHop = function(json) {
  var result = json;

  if(_(json).isPlainObject()) {
    var queries = processQueries(json.queries);
    var relations = processRelations(json.relations);

    result = {
      type: 'org.aksw.jena_sparql_api.hop.Hop',
      ctor: [ queries, relations ]
    };
  }
  return result;
};



/**
 * The json is interpreted as follows:
 * - string: a partitionedQueried to be executed on the parent service
 * - object: a single definition of a query to be run on a certain service
 * - array: if the array has either 1 or 2 string arguments it is treated similar to object (first argument is the the query, second the service)
 *   - otherwise: an array of query definitions
 *
 * If queries is an array, it can mean two things:
 *
 *
 * queries must be extended to an array of objects with attributes
 * - query : PartitionedQuery
 * - on    : SparqlService
 *
 * As a shortcut, the array can be omitted.
 *
 * @param json
 * @return
 */
var processQueries = function(json) {
    var isShortcut = isQueryShortcut(json);
    var queries = isShortcut ? [json] : (json ? json : []);

    var result = queries.map(function(query) {
        var r = processQuery(query);
        return r;
    });

    return result;
};


var isQueryShortcut = function(json) {
  var result = false;
  if(_(json).isArray()) {
    if(json.length <= 2) {
      var a = json[0];
      var b = json[1];

      if(!_(a).isObject() && _(b).isObject()) {
        result = true;
     }
   }
  }
  return result;
};



/**
 * two or three argument versions:
 * [ relation, hops ]
 * [ relation, service, hops ]
 *
 * (hops must be an array)
 *
 * @param json
 * @return
 */
var isRelationShortcut = function(json) {
  var result = false;
  if(_(json).isArray()) {

    if(json.length <= 3) {
      var a = json[0];
      var b = json[1];

      if(!_(a).isObject() && !_(b).isObject()) {
        result = true;
      }
    }
  }
  return result;
};




var processQuery = function(json) {
  var tmp;
  if(!_(json).isObject()) {
    tmp = {
      query: json
    };
  } else if(_(json).isArray()) {
    tmp = {
      query: json[0],
      on: json[1]
    };
  } else {
    tmp = json;
  }
  var result = processQueryCore(tmp);
  return result;
};

/**
 * A query object must provide the attributes
 * - query : PartitionedQuery
 * - on    : SparqlService (optional)
 * @param json
 * @return
 */
var processQueryCore = function(json) {
  var result = {
    type: 'org.aksw.jena_sparql_api.hop.HopQuery',
    ctor: [json.query, json.on]
  };

  return result;
};

var expandRelationShortuct = function(arr) {
  var relation = arr[0];
  var on;
  var hops;
  if(arr.length == 2) {
    on = null;
    hops = arr[1] || [];
  } else { // arr.length == 3
    on = arr[1] || [];
    hops = arr[2] || [];
  }
  var result = {
    via: relation,
    on: on,
    hops: hops
  };
  return result;
};

var processRelations = function(json) {
  var isShortcut = isRelationShortcut(json);
  var relations = isShortcut ? [json] : (json || []);
  var result = relations.map(function(relation) {
    var r = processRelation(relation);
    return r;
  });
  return result;
};


var processRelation = function(json) {
  var tmp = _(json).isArray() ? expandRelationShortuct(json) : json;
  var result = processRelationCore(tmp);
  return result;
};


var processRelationCore = function(json) {
  var hops = processHop(json.hops);

  var result = {
    type: 'org.aksw.jena_sparql_api.hop.HopRelation',
    ctor: [json.on, json.via, hops]
  };

  return result;
};

rewriters.push(function(json) {
  var result = json;
  var e = json.$hop;
  if(e) {
      result = processHop(e);
      print("JUP: " + JSON.stringify(result));
  }

  return result;
});



