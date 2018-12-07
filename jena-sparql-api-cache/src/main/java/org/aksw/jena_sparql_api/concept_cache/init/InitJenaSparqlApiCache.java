package org.aksw.jena_sparql_api.concept_cache.init;

import org.aksw.jena_sparql_api.concept_cache.core.JenaExtensionViewMatcher;
import org.apache.jena.sys.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitJenaSparqlApiCache
    implements JenaSubsystemLifecycle
{
    private static final Logger logger = LoggerFactory.getLogger(InitJenaSparqlApiCache.class);

    public void start() {
        logger.debug("JSA Cache initialization");
        JenaExtensionViewMatcher.register();
    }

    @Override
    public void stop() {
    }
}
