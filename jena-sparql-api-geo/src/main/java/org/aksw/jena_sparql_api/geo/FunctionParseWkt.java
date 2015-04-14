package org.aksw.jena_sparql_api.geo;

import com.google.common.base.Function;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;


public class FunctionParseWkt
    implements Function<String, Geometry>
{
    public static final FunctionParseWkt fn = new FunctionParseWkt();

    private WKTReader wktReader;

    public FunctionParseWkt() {
        this(new WKTReader());
    }

    public FunctionParseWkt(GeometryFactory geometryFactory) {
        this(new WKTReader(geometryFactory));
    }

    public FunctionParseWkt(WKTReader wktReader) {
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

    public static FunctionParseWkt create() {
        return new FunctionParseWkt();
    }

    public static FunctionParseWkt create(GeometryFactory geometryFactory) {
        return new FunctionParseWkt(geometryFactory);
    }

    public static FunctionParseWkt create(WKTReader wktReader) {
        return new FunctionParseWkt(wktReader);
    }
}
