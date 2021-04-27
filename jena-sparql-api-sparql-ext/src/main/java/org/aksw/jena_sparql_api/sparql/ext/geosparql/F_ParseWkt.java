package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

/** TODO This should either become a datatype or a real jena function */
public class F_ParseWkt implements Function<String, Geometry> {
	public static final F_ParseWkt fn = new F_ParseWkt();

	private WKTReader wktReader;

	public F_ParseWkt() {
		this(new WKTReader());
	}

	public F_ParseWkt(GeometryFactory geometryFactory) {
		this(new WKTReader(geometryFactory));
	}

	public F_ParseWkt(WKTReader wktReader) {
		this.wktReader = wktReader;
	}

	@Override
	public Geometry apply(String wktStr) {
		Geometry result;
		try {
			result = wktReader.read(wktStr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public static F_ParseWkt create() {
		return new F_ParseWkt();
	}

	public static F_ParseWkt create(GeometryFactory geometryFactory) {
		return new F_ParseWkt(geometryFactory);
	}

	public static F_ParseWkt create(WKTReader wktReader) {
		return new F_ParseWkt(wktReader);
	}
}
