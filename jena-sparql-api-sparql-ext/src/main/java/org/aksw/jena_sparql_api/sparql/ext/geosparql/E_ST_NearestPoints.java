package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;

public class E_ST_NearestPoints
	extends BinaryGeometryFunctionBase
{
	@Override
	public Geometry eval(Geometry a, Geometry b) {
		Coordinate[] coordinates = DistanceOp.nearestPoints(a, b);
//
////            Point ps = GeometryFactory.createPointFromInternalCoord(points[0], gs);
////            Point po = GeometryFactory.createPointFromInternalCoord(points[1], go);
//
        LineString result = gf.createLineString(coordinates);
        return result;
	}	
}
