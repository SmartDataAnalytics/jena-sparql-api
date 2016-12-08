rewriters.push(function(json) {
  var result = json;

  if(_(json).isPlainObject()) {
    if(json.type && ! json.beanClassName) {
        result = {};
        _(json).forEach(function(v, k) {
            if(k === 'type') {
                result.beanClassName = v;
            } else {
                result[k] = v;
            }
        });
    }
  }

  return result;
});
