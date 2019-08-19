package org.aksw.jena_sparql_api.user_defined_function.plugin;


import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctionResource;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginUdf
	implements JenaSubsystemLifecycle
{
	public void start() {
		init();
	}

	@Override
	public void stop() {
	}

	public static void init() {
		JenaPluginUtils.scan(UserDefinedFunctionResource.class);		
	}
	
	public static void init(Personality<RDFNode> p) {
	}
}
