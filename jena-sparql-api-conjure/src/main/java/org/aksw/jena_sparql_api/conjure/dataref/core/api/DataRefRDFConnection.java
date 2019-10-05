package org.aksw.jena_sparql_api.conjure.dataref.core.api;

/**
 * This deprecated class marks a memorial for future generations about
 * how to not design dataset-centric APIs:
 * 
 * One establishes a connection to a dataset, but a connection is not a dataset.
 * Or: A connection is just a tool to access (read/update) a dataset.
 *
 */
@Deprecated
public interface DataRefRDFConnection
{
//	RDFConnection getConnection();
//	
//	@Override
//	default <T> T accept(DataRefVisitor<T> visitor) {
//		T result = visitor.visit(this);
//		return result;
//	}
}
