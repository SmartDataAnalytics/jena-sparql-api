package org.aksw.jena_sparql_api.anyresource;

import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginAnyResource
    implements JenaSubsystemLifecycle
{

    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        BuiltinPersonalities.model.add(AnyResource.class, AnyResourceImpl.factory);
    }
}