package org.aksw.jena_sparql_api.conjure.dataobject.impl;

import org.aksw.jena_sparql_api.conjure.dataobject.api.DataObjectRdf;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

public class DataObjects {
	public static DataObjectRdf fromModel(Model model) {
		Dataset dataset = DatasetFactory.wrap(model);
		RDFConnection conn = RDFConnectionFactory.connect(dataset);
		
		return new DataObjectRdfBase(conn) {
			public void release() {
				conn.close();
			};
		};
	}


}
