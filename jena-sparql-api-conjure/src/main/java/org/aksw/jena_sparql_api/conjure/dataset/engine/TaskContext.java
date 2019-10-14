package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Map;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRef;
import org.apache.jena.rdf.model.Resource;

/**
 * TODO Probably this class should also be turned into a Resource
 * 
 * @author raven
 *
 */
public class TaskContext {
	protected Resource inputRecord;
	protected Map<String, DataRef> dataRefMapping;
	
	public TaskContext(Resource inputRecord, Map<String, DataRef> dataRefMapping) {
		super();
		this.inputRecord = inputRecord;
		this.dataRefMapping = dataRefMapping;
	}

	public Resource getInputRecord() {
		return inputRecord;
	}

	public Map<String, DataRef> getDataRefMapping() {
		return dataRefMapping;
	}
}
