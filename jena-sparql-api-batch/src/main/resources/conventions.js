conventions.add('auto type sparql endpoint', function(beanDef) {
	if(beanDef.type != null && beanDef.serviceUri) {
		beanDef.type = ''
	}
});
