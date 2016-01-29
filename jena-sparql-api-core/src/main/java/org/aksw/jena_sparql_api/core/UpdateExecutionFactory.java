package org.aksw.jena_sparql_api.core;

import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public interface UpdateExecutionFactory {
    UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest);
    UpdateProcessor createUpdateProcessor(String updateRequestStr);
}
