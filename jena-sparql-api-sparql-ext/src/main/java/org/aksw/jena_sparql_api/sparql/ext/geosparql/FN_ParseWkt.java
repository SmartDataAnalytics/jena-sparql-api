package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.function.Function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

/** TODO This should either become a datatype or a real jena function */
public class FN_ParseWkt implements Function<String, Geometry> {
	public static final FN_ParseWkt fn = new FN_ParseWkt();

	private WKTReader wktReader;

	public FN_ParseWkt() {
		this(new WKTReader());
	}

	public FN_ParseWkt(GeometryFactory geometryFactory) {
		this(new WKTReader(geometryFactory));
	}

	public FN_ParseWkt(WKTReader wktReader) {
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

	public static FN_ParseWkt create() {
		return new FN_ParseWkt();
	}

	public static FN_ParseWkt create(GeometryFactory geometryFactory) {
		return new FN_ParseWkt(geometryFactory);
	}

	public static FN_ParseWkt create(WKTReader wktReader) {
		return new FN_ParseWkt(wktReader);
	}
}
