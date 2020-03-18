package org.aksw.jena_sparql_api.conjure.dataset.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * TODO Probably this class should also be turned into a Resource
 * 
 * @author raven
 *
 */
public class TaskContext
	implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// TODO Clarify whether we need the input record as a resource or rather as a model
	// in ctxDatasets
	protected Resource inputRecord;
	
	// TODO Consider using a resource to back the map
	//protected Map<String, DataRef> dataRefMapping;
	protected Map<String, Op> dataRefMapping;
	
	/**
	 * Context models; right now this is only the input record, but
	 * it allows for extension with other models should the need arise
	 * 
	 */
	protected Map<String, Model> ctxModels;
	
	public TaskContext(
			Resource inputRecord,
			Map<String, Op> dataRefMapping,
			Map<String, Model> ctxModels) {
		super();
		this.inputRecord = inputRecord;
		this.dataRefMapping = dataRefMapping;
		this.ctxModels = ctxModels;
	}

	public Resource getInputRecord() {
		return inputRecord;
	}

	public Map<String, Op> getDataRefMapping() {
		return dataRefMapping;
	}

	public Map<String, Model> getCtxModels() {
		return ctxModels;
	}
	
	public static TaskContext empty() {
		return new TaskContext(
				ModelFactory.createDefaultModel().createResource(),
				new HashMap<>(),
				new HashMap<>());
	}
}
