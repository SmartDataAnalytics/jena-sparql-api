package org.aksw.jena_sparql_api.geo;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggTransform;
import org.aksw.jena_sparql_api.mapper.AggUtils;
import org.aksw.jena_sparql_api.mapper.FunctionNodeToString;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.RDF;

import com.vividsolutions.jts.geom.Geometry;

public class GeoMapSupplierUtils {

    public static final Var vs = Var.alloc("s");
    public static final Var vw = Var.alloc("w");
    public static final Var va = Var.alloc("a");

    public static final Var vx = Var.alloc("x");
    public static final Var vy = Var.alloc("y");


    public static Concept createConceptProperty(Node propertyNode) {

        BasicPattern pattern = new BasicPattern();
        pattern.add(new Triple(vs, propertyNode, vw));

        ElementTriplesBlock element = new ElementTriplesBlock(pattern);
        Concept result = new Concept(element, vs);
        return result;
    }

    public static Concept createConcept2(Node propertyNode1, Node propertyNode2) {

        BasicPattern pattern = new BasicPattern();
        pattern.add(new Triple(vs, propertyNode1, va));
        pattern.add(new Triple(va, propertyNode2, vw));

        ElementTriplesBlock element = new ElementTriplesBlock(pattern);
        Concept result = new Concept(element, vs);
        return result;
    }


    public static final Agg<Geometry> createAggregatorWkt(Var wktVar) {
        Agg<Geometry> result =
            AggTransform.create(
                AggTransform.create(AggUtils.literalNode(new ExprVar(wktVar)),
                        FunctionNodeToString.fn),
                FN_ParseWkt.fn);

        return result;
    }


    // TODO
    public static final Agg<Geometry> createAggregatorWgs84(Var lonVar, Var latVar) {

        ExprList args = new ExprList();
        args.add(NodeValue.makeString("POINT("));
        args.add(new E_Str(new ExprVar(lonVar)));
        args.add(NodeValue.makeString(" "));
        args.add(new E_Str(new ExprVar(latVar)));
        args.add(NodeValue.makeString(")"));
        Expr concat = new E_StrConcat(args);

        // "CONCAT('POINT(', STR('" + lonVar + "'), STR(" + latVar + "))"
        Agg<Geometry> result =
            AggTransform.create(
                AggTransform.create(AggUtils.literalNode(concat),
                        FunctionNodeToString.fn),
                FN_ParseWkt.fn);

        return result;
    }

//    public static final Function<ResultSet, NodeValue> createAggregatorWkt(Var wktVar) {
//        Aggregator aggregator = new AggLiteral(new ExprVar(wktVar));
//
//        Function<ResultSet, NodeValue> result = new FunctionResultSetAggregate(aggregator);
//
//        return result;
//    }

    public static final Concept conceptWgs84 = Concept.create("?s <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?x ; <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?y", "s");
    public static final Concept conceptWgsGeometry = createConceptProperty(NodeFactory.createURI("http://www.w3.org/2003/01/geo/wgs84_pos#geometry"));
    public static final Concept conceptOgcGeometry = createConcept2(NodeFactory.createURI("http://geovocab.org/geometry#geometry"), NodeFactory.createURI("http://www.opengis.net/ont/geosparql#asWKT"));


    public static final MappedConcept<Geometry> mcWgsGeometry = MappedConcept.create(conceptWgsGeometry, createAggregatorWkt(vw));
    public static final MappedConcept<Geometry> mcWgs84 = MappedConcept.create(conceptWgs84, createAggregatorWgs84(vx, vy));
    public static final MappedConcept<Geometry> mcOgcGeometry = MappedConcept.create(conceptOgcGeometry, createAggregatorWkt(vw));



    public static final String wktLiteralStr = "http://www.opengis.net/ont/geosparql#wktLiteral";
    public static final RDFDatatype geomType = TypeMapper.getInstance().getSafeTypeByName(wktLiteralStr);

    public static final String geomTypeVirtStr = "http://www.openlinksw.com/schemas/virtrdf#Geometry";
    public static final RDFDatatype geomTypeVirt = TypeMapper.getInstance().getSafeTypeByName(geomTypeVirtStr);


    /**
     * Create reified statements from the triple
     *
     * @param target
     * @param map
     */
    public static Set<Triple> geomizedToRdf(Map<Triple, Geometry> map) {
        Set<Triple> result = new HashSet<Triple>();

        Node Link = NodeFactory.createURI("http://www.linklion.org/ontology#Link");
        Node ogcAsWkt = NodeFactory.createURI("http://www.opengis.net/ont/geosparql#asWKT");

        for(Entry<Triple, Geometry> entry : map.entrySet()) {
            Triple t = entry.getKey();

            String uri = "http://example.org/link-" + StringUtils.md5Hash(TripleUtils.toNTripleString(t));
            Node s = NodeFactory.createURI(uri);

            Node g = NodeFactory.createLiteral(entry.getValue().toText(), geomType);

            result.add(new Triple(s, RDF.type.asNode(), Link));
            result.add(new Triple(s, RDF.subject.asNode(), t.getSubject()));
            result.add(new Triple(s, RDF.predicate.asNode(), t.getPredicate()));
            result.add(new Triple(s, RDF.object.asNode(), t.getObject()));
            result.add(new Triple(s, ogcAsWkt, g));
        }

        return result;
    }

    /**
     * Function to convert the datatype from
     * http://www.opengis.net/ont/geosparql#wktLiteral
     * to http://www.openlinksw.com/schemas/virtrdf#Geometry
     *
     * @param t
     */
    public static Triple convertOgcToVirt(Triple t) {
        Triple result = t;

        Node o = t.getObject();
        if(o.isLiteral()) {
            String dt = o.getLiteralDatatypeURI();
            if(dt.equals(wktLiteralStr)) {

                Node newO = NodeFactory.createLiteral(o.getLiteralLexicalForm(), geomTypeVirt);
                result = new Triple(t.getSubject(), t.getPredicate(), newO);
            }
        }


        return result;
    }

    public static Set<Triple> convertOgcToVirt(Set<Triple> ts) {
        Set<Triple> result = ts.stream().map(fnConvertOgcToVirt).collect(Collectors.toSet());
        return result;
    }

    public static Function<Triple, Triple> fnConvertOgcToVirt = new Function<Triple, Triple>() {
        @Override
        public Triple apply(Triple t) {
            Triple result = convertOgcToVirt(t);
            return result;
        }
    };

    //public static
}