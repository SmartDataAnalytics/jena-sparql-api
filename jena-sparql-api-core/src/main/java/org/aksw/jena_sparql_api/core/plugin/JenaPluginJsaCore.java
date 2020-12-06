package org.aksw.jena_sparql_api.core.plugin;

import org.aksw.jena_sparql_api.core.connection.RDFConnectionMetaData;
import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.apache.jena.sys.JenaSystem;

public class JenaPluginJsaCore
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(RDFConnectionMetaData.class);
    }

    public static void init(Personality<RDFNode> p) {
    }
}
