package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

public class GeoFunctionsJena {
	public static Geometry simplifyDp(Geometry geom, double tolerance, boolean ensureValid) {
		DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(geom);
		simplifier.setDistanceTolerance(0);
		simplifier.setEnsureValid(ensureValid);
		Geometry result = simplifier.getResultGeometry();
		return result;
	}
}
