package org.aksw.jena_sparql_api.web.server.spring_boot_playground;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig
    extends ResourceConfig
{
    public JerseyConfig() {
//        register(componentClass)
//        packages("...");
//        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}
