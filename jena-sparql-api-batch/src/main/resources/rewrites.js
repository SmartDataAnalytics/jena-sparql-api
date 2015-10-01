
var simpleMappers = {};


var simpleMappers.$sparqlFile = function(json) {
    var result = {
        test: foo;
    };
};


Object.keys = function() {
    var result = [];

    for (var k in this) {
        if (this.hasOwnProperty(k)) {
            result.push(k);
        }
    }

    return result;
}


var applyRewrites = function(json) {
    var keys = Object.keys(json);

    // By default set json as the result
    var result = json;

    if(keys.length === 1) {
        var key = keys[0];
        var val = json[key];

        var fn = simpleMappers[key];
        if(fn) {
            result = fn(val);
        }
    }

    return result;
}
