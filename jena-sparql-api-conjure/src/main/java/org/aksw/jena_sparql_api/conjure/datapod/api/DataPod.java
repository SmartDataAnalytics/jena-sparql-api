package org.aksw.jena_sparql_api.conjure.datapod.api;

/**
 * A DataPod represents a specific digital copy of a dataset
 * and acts as the provider for means of access to it.
 *
 * Technically, a DataPod is a possibly mutable logical view over a (sub set of a) database system
 * 
 * @author raven
 *
 */
public interface DataPod
	extends AutoCloseable
{
	//List<Checksum> getContentHashes();
	
	//Resource persist(Path baseFolder);
	//Persister getPersister();
	// Simple persist method
	/**
	 * In general, it seems the the data pod is not a bad place for persistence
	 * functionality.
	 * 
	 * But there are two types of persistence: On the dataset level and the service level.
	 * Assume a virtuoso, that persists its state as a virtuoso.db file.
	 * If we wanted to persist a virtuso.db file, we'd have to stop the service, copy the file,
	 * and restart it.
	 * So we might actually do something like dataPod.getService().persistState()
	 * 
	 * @param path The file to write to (if persist creates an archive, it is the directory)
	 * @return A resource describing the content type, encoding written
	 * @throws IOException
	 */
	//RdfEntityInfo persist(Path path) throws IOException;

	
	/**
	 * Whether the content of the data object can be modified
	 * 
	 */
	boolean isMutable();

	// Note: See notes at the end of the file for current state of considerations
	// Set<Class<?>> listMeansOfAccess();
	// <T> T getAccessVia(Class<T> clazz, boolean allowDerived);
	// <T> T hasAccessVia(Class<T> clazz, boolean allowDerived);
	
	/**
	 * Indicate that the data pod is no longer needed
	 * 
	 * 
	 * DataPod x = factory.create(spec);
	 * x.release(); // Behavior depends on the used factory
	 */
}


// A data object may provide different means of access - for example Model and RDFConnection
// The DataObject should only expose its 'native' means of access -
// it should not itself convert between means of access

// So a data object backed by a Model can also expose an RDFConnection because for all
// operations that can be done on the model, doing them with SPARQL does not require any
// effort besides the constant overhead of parsing sparql queries
// compared to going directly to the graph api - the connection is a lightweight wrapper for the model


// Conversely, using a graph to wrap a connection will typcially have vastly different performance characteristics
// because the graph only supports triple pattern lookups
// So in this case the wrapper will impose non-constant overhead
// by causing query planning to happen in the wrapper instead of pushing it to the connection's backend
	
// Above statements still need to be sharpend, but in the end I think its about the performance penalty
// Only APIs with at most a constant overhead (independent of the data) should be provided natively
// So a request for a Model that triggers a transfer of all data from a connection is certainly
// ruled out - these kind of transformations should be explicitly stated using
// conjure dataset ops, something like OpClone(subOp, storageHint="memory")

// That said, it may still be convenient to be able to request non optimal means of access in order to get a job done
// Without the need to go via some separate manager
// Maybe we could account for that with a flag that indicates
// whether only native or also derived means of access should be returned

// Means of access can be connection-like - i.e. something that may have to be configured, opened and closed
// The question is how to treat other APIs, such as Model
// Is this two types of means of access that need to be treated separately?
// Or does getAccessor(RDFConnection.class) return an opened connection, and the documented contract for
// it is that it always needs to be closed after use? 
// getAccessVia(Model.class)
// getAccessVia(RDFConnection.class)