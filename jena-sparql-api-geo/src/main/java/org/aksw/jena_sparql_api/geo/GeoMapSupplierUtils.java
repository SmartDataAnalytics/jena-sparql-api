package org.aksw.jena_sparql_api.geo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.mapper.Agg;
import org.aksw.jena_sparql_api.mapper.AggTransform;
import org.aksw.jena_sparql_api.mapper.AggUtils;
import org.aksw.jena_sparql_api.mapper.FunctionNodeToString;
import org.aksw.jena_sparql_api.mapper.MappedConcept;
import org.aksw.jena_sparql_api.utils.TripleUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.vividsolutions.jts.geom.Geometry;

public class GeoMapSupplierUtils {
    
    public static final Var vs = Var.alloc("s");
    public static final Var vw = Var.alloc("w");
    public static final Var va = Var.alloc("a");

    
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


    public static final Agg<String> createAggregatorWkt(Var wktVar) {
        Agg<String> result =
            AggTransform.create(AggUtils.literal(new ExprVar(wktVar)),
                FunctionNodeToString.fn);

        return result;
    }

//    public static final Function<ResultSet, NodeValue> createAggregatorWkt(Var wktVar) {
//        Aggregator aggregator = new AggLiteral(new ExprVar(wktVar));
//
//        Function<ResultSet, NodeValue> result = new FunctionResultSetAggregate(aggregator);
//        
//        return result;
//    }
    
    public static final Concept conceptWgsGeometry = createConceptProperty(NodeFactory.createURI("http://www.w3.org/2003/01/geo/wgs84_pos#geometry"));    
    public static final Concept conceptOgcGeometry = createConcept2(NodeFactory.createURI("http://geovocab.org/geometry#geometry"), NodeFactory.createURI("http://www.opengis.net/ont/geosparql#asWKT"));    

    
    public static final MappedConcept mcWgsGeometry = new MappedConcept(conceptWgsGeometry, createAggregatorWkt(vw));
    public static final MappedConcept mcOgcGeometry = new MappedConcept(conceptOgcGeometry, createAggregatorWkt(vw));

    

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
        Collection<Triple> tmp = Collections2.transform(ts, fnConvertOgcToVirt);
        Set<Triple> result = new HashSet<Triple>(tmp);
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