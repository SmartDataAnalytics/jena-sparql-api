package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.DimensionInfo;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.jts.CustomGeometryFactory;
import org.apache.jena.geosparql.implementation.parsers.wkt.WKTReader;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;

/** Datatype for Well Known Binary Literals */
public class RDFDatatypeWkbLiteral extends GeometryDatatype {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(RDFDatatypeWkbLiteral.class);

    protected WKBReader wkbReader;
    protected WKBWriter wkbWriter;
    
    // Writer for intermediate WKT representation in order to reuse Jena's WKT machinery
    protected WKTWriter wktWriter;
    
    /**
     * The default WKT type URI.
     */
    public static final String URI = GeoSPARQL_URI.GEO_URI + "wkbLiteral";

    /**
     * A static instance of WKTDatatype.
     */
    public static final RDFDatatypeWkbLiteral INSTANCE = new RDFDatatypeWkbLiteral();

    public RDFDatatypeWkbLiteral() {
    	this(URI);
    }

    public RDFDatatypeWkbLiteral(String uri) {
    	this(uri, new WKBReader(CustomGeometryFactory.theInstance()), new WKBWriter());
    }

    public RDFDatatypeWkbLiteral(String uri, WKBReader wkbReader, WKBWriter wkbWriter) {
        this(uri, wkbReader, wkbWriter, new WKTWriter());
    }

    
    public RDFDatatypeWkbLiteral(String uri, WKBReader wkbReader, WKBWriter wkbWriter, WKTWriter wktWriter) {
        super(uri);
        this.wkbReader = wkbReader;
        this.wkbWriter = wkbWriter;
        this.wktWriter = wktWriter;
    }
    
    

    /**
     * This method Un-parses the JTS Geometry to the WKT literal
     *
     * @param geometry - the JTS Geometry to be un-parsed
     * @return WKT - the returned WKT Literal.
     * <br> Notice that the Spatial Reference System is not specified in
     * returned WKT literal.
     *
     */
    @Override
    public String unparse(Object geometry) {
        if (geometry instanceof GeometryWrapper) {
            GeometryWrapper geometryWrapper = (GeometryWrapper) geometry;
            byte[] bytes = wkbWriter.write(geometryWrapper.getParsingGeometry());
        	String str = BaseEncoding.base64().encode(bytes);
            return str;
        } else {
            throw new DatatypeFormatException("Object to unparse WKBDatatype is not a GeometryWrapper: " + geometry);
        }
    }

    @Override
    public GeometryWrapper read(String geometryLiteral) {

    	byte[] bytes = BaseEncoding.base64().decode(geometryLiteral);
    	Geometry geometry;
		try {
			geometry = wkbReader.read(bytes);
			String tmpGeoStr = wktWriter.write(geometry);
	        WKTReader wktReader = WKTReader.extract(tmpGeoStr);
	        String srsURI = wktReader.getSrsURI();
	        DimensionInfo dimensionInfo = wktReader.getDimensionInfo();

	        return new GeometryWrapper(geometry, srsURI, WKTDatatype.URI, dimensionInfo, tmpGeoStr);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}    	
    }

    @Override
    public String toString() {
        return "WKTDatatype{" + URI + '}';
    }

}