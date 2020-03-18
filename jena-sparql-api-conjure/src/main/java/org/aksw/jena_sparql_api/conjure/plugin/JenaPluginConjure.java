package org.aksw.jena_sparql_api.conjure.plugin;

import org.aksw.dcat.ap.domain.api.Checksum;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.aksw.jena_sparql_api.conjure.job.api.Job;
import org.aksw.jena_sparql_api.conjure.resourcespec.ResourceSpec;
import org.aksw.jena_sparql_api.conjure.traversal.api.OpTraversal;
import org.aksw.jena_sparql_api.http.domain.api.RdfEntityInfoDefault;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.utils.turtle.TurtleWriterNoBase;
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
		TurtleWriterNoBase.register();
		JenaPluginHdt.init();

		JenaPluginUtils.scan(Job.class);
		JenaPluginUtils.scan(OpTraversal.class);
		JenaPluginUtils.scan(RdfEntityInfoDefault.class);
		JenaPluginUtils.scan(Checksum.class);
		JenaPluginUtils.scan(DataRef.class);
		JenaPluginUtils.scan(org.aksw.jena_sparql_api.conjure.entity.algebra.Op.class);
		JenaPluginUtils.scan(org.aksw.jena_sparql_api.conjure.dataset.algebra.Op.class);
		JenaPluginUtils.scan(ResourceSpec.class);
	}
}
