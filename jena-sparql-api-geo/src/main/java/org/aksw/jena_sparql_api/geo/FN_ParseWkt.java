package org.aksw.jena_sparql_api.geo;

import java.util.function.Function;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;


public class FN_ParseWkt
    implements Function<String, Geometry>
{
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
