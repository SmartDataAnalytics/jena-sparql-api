package org.aksw.jena_sparql_api.sparql.ext.gml;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLReader;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class E_Gml2Wkt extends FunctionBase1 {

    private GMLReader gmlReader = new GMLReader();
    private WKTWriter wktWriter = new WKTWriter();
    private GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public NodeValue exec(NodeValue nodeValue) {
        NodeValue result = Expr.NONE.getConstant();
        if (nodeValue.isString()) {
            try {
                Geometry geometry = gmlReader.read(nodeValue.getString(), geometryFactory);
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
