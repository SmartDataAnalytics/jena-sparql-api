package org.aksw.jena_sparql_api.path.plugin;

import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePPath;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginPath
    implements JenaSubsystemLifecycle {

    public void start() {
        init();
    }

    @Override
    public void stop() {
    }


    public static void init() {
        TypeMapper.getInstance().registerDatatype(RDFDatatypePPath.INSTANCE);
    }
}
