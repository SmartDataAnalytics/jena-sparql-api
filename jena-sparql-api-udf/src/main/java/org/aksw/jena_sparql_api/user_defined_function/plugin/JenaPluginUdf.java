package org.aksw.jena_sparql_api.user_defined_function.plugin;


import org.aksw.jena_sparql_api.mapper.proxy.JenaPluginUtils;
import org.aksw.jena_sparql_api.user_defined_function.InverseDefinition;
import org.aksw.jena_sparql_api.user_defined_function.PrefixDefinition;
import org.aksw.jena_sparql_api.user_defined_function.PrefixSet;
import org.aksw.jena_sparql_api.user_defined_function.UdfDefinition;
import org.aksw.jena_sparql_api.user_defined_function.UdpfDefinition;
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
        JenaPluginUtils.registerResourceClasses(
                UserDefinedFunctionResource.class,
                InverseDefinition.class,
                PrefixDefinition.class,
                PrefixSet.class,
                UdfDefinition.class
                );
    }

    public static void init(Personality<RDFNode> p) {
    }
}
