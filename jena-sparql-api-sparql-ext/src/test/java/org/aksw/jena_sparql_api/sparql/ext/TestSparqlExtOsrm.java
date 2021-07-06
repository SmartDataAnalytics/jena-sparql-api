package org.aksw.jena_sparql_api.sparql.ext;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.JenaExtensionDuration;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.query.*;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

public class TestSparqlExtOsrm {
	
	@Test
	public void testOsrm() {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Standard.getNsPrefixMap());
		JenaExtensionDuration.addPrefixes(pm);

		Query q = QueryFactory.create(
				"PREFIX  geo:  <http://www.opengis.net/ont/geosparql#>" +
				" PREFIX osrm: <http://jsa.aksw.org/fn/osrm/>" +
				" PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ " SELECT * WHERE {" +
						"(?route ?distance ?duration) osrm:query (<http://localhost:5000/route/v1/driving/> 'Point(8.24 50.0825)'^^geo:wktLiteral 'Point(13.74 51.05)'^^geo:wktLiteral) }");

		ResultSet rs = QueryExecutionFactory.create(q, DatasetFactory.create()).execSelect();
		ResultSetFormatter.out(rs);
	}

}
