package org.aksw.jena_sparql_api.core;

import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

public interface UpdateExecutionFactory {
    UpdateProcessor createUpdateProcessor(UpdateRequest updateRequest);
    UpdateProcessor createUpdateProcessor(String updateRequestStr);
}
