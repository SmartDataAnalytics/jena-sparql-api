package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultValue;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

public class GeoFunctionsJena {
	
	@IriNs(GeoSPARQL_URI.GEOF_URI)
	public static Geometry simplifyDp(
			Geometry geom,
			@DefaultValue("0") double tolerance,
			@DefaultValue("true") boolean ensureValid) {
		DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(geom);
		simplifier.setDistanceTolerance(tolerance);
		simplifier.setEnsureValid(ensureValid);
		Geometry result = simplifier.getResultGeometry();
		return result;
	}

	@IriNs(GeoSPARQL_URI.GEOF_URI)
	public static Geometry centroid(
			Geometry geom) {
		Geometry result = geom.getCentroid();
		return result;
	}

	@IriNs(GeoSPARQL_URI.GEOF_URI)
	public static double lat(Geometry geom) {
		if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
		return ((Point)geom).getX();
	}

	@IriNs(GeoSPARQL_URI.GEOF_URI)
	public static double lon(Geometry geom) {
		if (!(geom instanceof Point)) throw new ExprEvalException("not a point");
		return ((Point)geom).getY();
	}

}
