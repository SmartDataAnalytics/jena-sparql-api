package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.aksw.commons.collector.core.AggBuilder;
import org.aksw.commons.collector.domain.ParallelAggregator;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class AggregatorsJts {
	/** Creates an aggregator that collects geometries into a geometry collection */
	public static ParallelAggregator<Geometry, Geometry, ?> aggGeometryCollection(boolean distinct, GeometryFactory geomFactory) {
		SerializableSupplier<Collection<Geometry>> collectionSupplier = distinct
				? LinkedHashSet::new
				: ArrayList::new; // LinkedList?
		
		return AggBuilder.outputTransform(
			AggBuilder.collectionSupplier(collectionSupplier),
			col -> geomFactory.createGeometryCollection(col.toArray(new Geometry[0]))
			);
	}
}
