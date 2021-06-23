package org.aksw.jena_sparql_api.sparql.ext.duration;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.JenaExtensionDuration;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

public class TestSparqlExtDuration {
	
	@Test
	public void testMinutes() {
		PrefixMapping pm = new PrefixMappingImpl();
		pm.setNsPrefixes(PrefixMapping.Standard.getNsPrefixMap());
		JenaExtensionDuration.addPrefixes(pm);

		NodeValue nv = ExprUtils.eval(ExprUtils.parse("duration:asMinutes('PT10M60S'^^xsd:duration)", pm));
		System.out.println(nv);

		nv = ExprUtils.eval(ExprUtils.parse("duration:asSeconds('PT2H10M60S'^^xsd:duration)", pm));
		System.out.println(nv);

		nv = ExprUtils.eval(ExprUtils.parse("duration:asHours('PT1H70M3602S'^^xsd:duration)", pm));
		System.out.println(nv);

		nv = ExprUtils.eval(ExprUtils.parse("duration:simplify('P1DT30H70M3602S'^^xsd:duration)", pm));
		System.out.println(nv);
	}

}
