package org.aksw.jena_sparql_api.conjure.job.api;

import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A Job instance binds the placeholders/variables
 * of a job to concrete values
 * 
 * @author raven
 *
 */
public interface JobInstance
	extends Resource
{
	Job getJob();
	// These are variables that are substituted with literals
	Map<String, RDFNode> setEnvMap();
	Map<String, RDFNode> getEnvMap();

	// Mapping of OpVar variables - these are variables that are substituted by sub workflows
	// I.e. their evaluation yields datasets
	// TODO Maybe instead of OpVar - ResourceVar or so would be better
	// so extension points where a resource from another RDF graph (together with that graph)
	// can be injected
	Map<String, RDFNode> setOpVarMap();
	Map<String, RDFNode> getOpVarMap();

}
