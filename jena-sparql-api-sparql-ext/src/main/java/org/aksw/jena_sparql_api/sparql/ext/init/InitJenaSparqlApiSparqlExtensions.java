package org.aksw.jena_sparql_api.sparql.ext.init;

import org.aksw.jena_sparql_api.sparql.ext.csv.JenaExtensionCsv;
import org.aksw.jena_sparql_api.sparql.ext.json.JenaExtensionJson;
import org.aksw.jena_sparql_api.sparql.ext.xml.JenaExtensionXml;
import org.apache.jena.system.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitJenaSparqlApiSparqlExtensions
    implements JenaSubsystemLifecycle
{
    private static final Logger logger = LoggerFactory.getLogger(InitJenaSparqlApiSparqlExtensions.class);

    public void start() {
        logger.debug("JenaSparqlAPI SPARQL extension initialization");
        JenaExtensionJson.register();
        JenaExtensionCsv.register();
        JenaExtensionXml.register();
    }

    @Override
    public void stop() {
    }
}
