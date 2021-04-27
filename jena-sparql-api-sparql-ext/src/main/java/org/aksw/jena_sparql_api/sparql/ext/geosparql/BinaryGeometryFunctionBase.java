package org.aksw.jena_sparql_api.sparql.ext.geosparql;

import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BinaryGeometryFunctionBase
	extends FunctionBase2
{
	public abstract Geometry eval(Geometry a, Geometry b);

	private static final Logger logger = LoggerFactory.getLogger(BinaryGeometryFunctionBase.class);

	protected Function<String, Geometry> wktParser = new F_ParseWkt();
	protected Function<Geometry, String> wktWriter = new WKTWriter()::write;

    protected GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

//    public BinaryGeometryFunctionBase() {
//    	super()
//    }
    
    
    
	@Override
	public NodeValue exec(NodeValue a, NodeValue b) {
		// Try to parse the lexical forms as WKT

		String aStr = a.asNode().getLiteralLexicalForm();
		String bStr = b.asNode().getLiteralLexicalForm();

		NodeValue result;
		
		Geometry aGeo = null;
		Geometry bGeo = null;
		try {
			aGeo = wktParser.apply(aStr);
			bGeo = wktParser.apply(bStr);
		} catch(Exception e) {
			logger.warn("Error parsing provided argument as WKT", e);
		}

		if(aGeo != null && bGeo != null) {
			Geometry g = eval(aGeo, bGeo);
	
			RDFDatatype dtype = TypeMapper.getInstance().getSafeTypeByName("http://www.opengis.net/ont/geosparql#wktLiteral");
			
			String str = g != null ? wktWriter.apply(g) : null;
			result = str == null ? null : NodeValue.makeNode(NodeFactory.createLiteral(str, dtype));
		} else {
			result = null;
		}
		
		// if (result == null) { raise ExprEvalException().; } ?
		
		return result;
	}

}
