package org.aksw.jena_sparql_api.batch.processor;

import com.google.common.base.Function;
import com.hp.hpl.jena.sparql.path.Path;

/**
 * Item processor that generates paths
 * 
 * @author raven
 *
 */
public class FunctionGenPath
    implements Function<ResourceModel, ResourceModel>
{
    private Path path;
}
