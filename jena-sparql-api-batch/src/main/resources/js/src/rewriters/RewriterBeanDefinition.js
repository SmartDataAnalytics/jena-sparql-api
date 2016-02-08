/**
 * If a json document which does not have a properties key, all keys which are not part of Generic Bean definition will be moved to
 * the properties section
 *
 * {
 *   type: 'some.java.class.name',
 *   nonBeanDefinitionKey: 'foo'
 * }
 *
 * {
 *   type: 'some.java.class.name',
 *   properties: {
 *     nonBeanDefinitionKey: 'foo'
 *   }
 * }
 *
 * @author raven
 *
 */
rewriters.push(function(json) {
  var result = json;

  var subDocKey = 'properties';
  var isTransferNeeded = function(item) {
      var arr = [];
      var r = _(arr).includes(item);
      return r;
  };


  if(_(json).isPlainObject()) {
    if(json.beanClassName && !json[subDocKey]) {
      result = {};
      var tmp = {};
      _(json).forEach(function(v, k) {
          var move = isTransferNeeded.apply(k);
          if(move) {
            tmp[k] = v;
          } else {
            result[k] = v;
          }
        });

      result[subDocKey] = tmp;
    }
  }

  return result;
});
