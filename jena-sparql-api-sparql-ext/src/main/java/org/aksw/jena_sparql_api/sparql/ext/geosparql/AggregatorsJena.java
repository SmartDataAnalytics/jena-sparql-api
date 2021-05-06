package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.Aggregator;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class AggregatorsJena {

	public static Geometry extractGeometryOrNull(NodeValue nv) {
		Geometry result = null;
		try {
			GeometryWrapper wrapper = GeometryWrapper.extract(nv);
			result = wrapper.getParsingGeometry();
		} catch (Exception e) {
			// Nothing to do
		}
		
		return result;
	}
	
	public static Aggregator<Binding, NodeValue> aggGeometryCollection(Expr geomExpr, boolean distinct) {
		return aggGeometryCollection(geomExpr, distinct, CustomGeometryFactory.theInstance());
	}
	
	public static Aggregator<Binding, NodeValue> aggGeometryCollection(
			Expr geomExpr,
			boolean distinct,
			GeometryFactory geomFactory) {
		
		// TODO This approach silently ignores invalid input
		// We should probably instead yield effectively 'null' 
		return
			AggBuilder.outputTransform(
				AggBuilder.inputTransform(
						(Binding binding) -> {
							NodeValue nv = geomExpr.eval(binding, null);
							return AggregatorsJena.extractGeometryOrNull(nv);
						},
						AggBuilder.inputFilter(input -> input != null,
							AggregatorsJts.aggGeometryCollection(distinct, geomFactory))),
				(Geometry geom) -> new GeometryWrapper(geom, WKTDatatype.URI).asNodeValue());
	}
}
