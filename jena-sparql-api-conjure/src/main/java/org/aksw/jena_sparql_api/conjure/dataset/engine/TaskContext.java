package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.util.Map;

import org.apache.jena.rdf.model.Resource;

/**
 * TODO Probably this class should also be turned into a Resource
 * 
 * @author raven
 *
 */
public class TaskContext {
	protected Resource inputRecord;
	protected Map<String, Resource> dataRefMapping;
	
	public TaskContext(Resource inputRecord, Map<String, Resource> dataRefMapping) {
		super();
		this.inputRecord = inputRecord;
		this.dataRefMapping = dataRefMapping;
	}

	public Resource getInputRecord() {
		return inputRecord;
	}

	public Map<String, Resource> getDataRefMapping() {
		return dataRefMapping;
	}
}
