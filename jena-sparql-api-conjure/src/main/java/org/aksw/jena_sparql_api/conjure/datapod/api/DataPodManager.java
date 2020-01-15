package org.aksw.jena_sparql_api.conjure.datapod.api;


// TODO Clarify relation between a DataPodManager and a QuadDataPod
// So we should have explicit named graph support on the DataPod level
// If we had a QuadDataPod, we could for each named graph return an RdfDataPod
public interface DataPodManager {
	/**
	 * Allocate an anonymous datapod
	 * 
	 * For a quad-based triple store, this would simply
	 * allocate a new named graph (with some internal name)
	 * and wrap it as a DataPodSparqlEndpoint
	 * 
	 * @return
	 */
	DataPod newPod();
	
	
	/**
	 * Get access to the (typically immutable) pod of the data manager's
	 * metadata.
	 * 
	 * For example, for a quad store, this would be a virtual RDF graph
	 * of the set of named graphs
	 * 
	 * @return
	 */
	DataPod newMetadataPod();
	
	/**
	 * Return how many more datapods can be allocated before this resource
	 * is exhausted
	 * 
	 * @return
	 */
	int getRemainingCapacity();
}
