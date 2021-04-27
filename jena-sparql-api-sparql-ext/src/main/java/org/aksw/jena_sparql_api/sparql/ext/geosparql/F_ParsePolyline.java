package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;

/** Function that parses a poly line into a WKT literal; a linestring of all points */
public class F_ParsePolyline
	extends FunctionBase1
{
	protected GeometryFactory geometryFactory;
	
	public F_ParsePolyline() {
		this(CustomGeometryFactory.theInstance());
	}

	public F_ParsePolyline(GeometryFactory geometryFactory) {
		super();
		this.geometryFactory = geometryFactory;
	}


	public static Coordinate convert(LatLng ll) {
		return new Coordinate(ll.lng, ll.lat);
	}
	
	public static Coordinate[] convertAll(Collection<LatLng> ll) {
		Coordinate[] result = ll.stream()
				.map(F_ParsePolyline::convert)
				.collect(Collectors.toList())
				.toArray(new Coordinate[0]);
		return result;
	}
	
	@Override
	public NodeValue exec(NodeValue v) {
		if (v != null && v.isString()) {
			String str = v.getString();
			List<LatLng> lls = PolylineEncoding.decode(str);			
			Coordinate[] coords = convertAll(lls);
			Geometry geom = geometryFactory.createLineString(coords);
			GeometryWrapper gw = new GeometryWrapper(geom, WKTDatatype.URI);
			Node node = NodeFactory.createLiteralByValue(gw, WKTDatatype.INSTANCE);
			NodeValue result = NodeValue.makeNode(node);
			return result;
		} else {
			throw new ExprEvalException("ParsePolyline requires string argument");
		}
		
	}
}
