package org.aksw.dcat.jena.plugin;

import org.aksw.dcat.jena.domain.api.DcatDataset;
import org.aksw.dcat.jena.domain.api.DcatDistribution;
import org.aksw.dcat.jena.domain.api.MavenEntity;
import org.aksw.dcat.jena.domain.impl.DatasetImpl;
import org.aksw.dcat.jena.domain.impl.DcatDistributionImpl;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.utils.model.SimpleImplementation;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginDcat
	implements JenaSubsystemLifecycle {

	public void start() {
		JenaPluginDcat.init();
	}
	
	@Override
	public void stop() {
	}

	public static void init() {
		init(BuiltinPersonalities.model);		
	}
	
	public static void init(Personality<RDFNode> p) {
		JenaPluginUtils.registerResourceClasses(MavenEntity.class);
    	p.add(DcatDataset.class, new SimpleImplementation(DatasetImpl::new));
    	p.add(DcatDistribution.class, new SimpleImplementation(DcatDistributionImpl::new));
    }
}
