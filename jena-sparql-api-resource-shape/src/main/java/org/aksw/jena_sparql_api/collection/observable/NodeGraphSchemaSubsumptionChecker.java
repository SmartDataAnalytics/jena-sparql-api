package org.aksw.jena_sparql_api.collection.observable;

import org.aksw.jena_sparql_api.schema.NodeSchema;

public interface NodeGraphSchemaSubsumptionChecker {
    boolean isSubsumed(NodeSchema needle, NodeSchema haystack);
}
