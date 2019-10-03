package org.aksw.jena_sparql_api.conjure.plugin;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefResource;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginConjure
	implements JenaSubsystemLifecycle {

	public void start() {
		init();
	}

	@Override
	public void stop() {
	}

	
	public static void init() {
		JenaPluginUtils.scan(DataRefResource.class);
	}
}
