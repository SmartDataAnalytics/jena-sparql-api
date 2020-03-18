package org.aksw.jena_sparql_api.sparql.ext.gml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.geosparql.implementation.jts.GeometryTransformation;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.sis.referencing.CRS;
//import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jts.io.gml2.GMLReader;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;

public class E_Gml2Wkt extends FunctionBase1 {

    private GMLReader gmlReader = new GMLReader();
    private WKTWriter wktWriter = new WKTWriter();
    private GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public NodeValue exec(NodeValue nodeValue) {
        NodeValue result = Expr.NONE.getConstant();
        if (nodeValue.isString()) {
            try {
                String gml = nodeValue.getString();
                Geometry geometry = gmlReader.read(gml, geometryFactory);
                Pattern p = Pattern.compile("srsName=\\\"([^\"]*)\\\"");
                Matcher m = p.matcher(gml);
                if (m.find()) {
                    String srs = m.group(1);
                    CoordinateReferenceSystem crsSource = CRS.forCode(srs);
                    CoordinateReferenceSystem crsTarget = CRS.forCode("CRS:84");
                    CoordinateOperation operation = CRS.findOperation(crsSource, crsTarget, null);
                    MathTransform transform = operation.getMathTransform();
                    geometry = GeometryTransformation.transform(geometry, transform);
                    //CoordinateReferenceSystem crsSource = CRS.decode(srs);
//                    GeographicCRS crsTarget =
//                            org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
//                    MathTransform transform = CRS.findMathTransform(crsSource, crsTarget, false);
//                    geometry = JTS.transform(geometry, transform);
                    
                }
                RDFDatatype datatype = TypeMapper.getInstance()
                        .getSafeTypeByName("http://www.opengis.net/ont/geosparql#wktLiteral");
                String wktString = wktWriter.write(geometry);
                result = NodeValue.makeNode(NodeFactory.createLiteral(wktString, datatype));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
