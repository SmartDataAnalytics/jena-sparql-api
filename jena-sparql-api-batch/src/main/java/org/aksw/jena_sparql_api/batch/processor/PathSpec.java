package org.aksw.jena_sparql_api.batch.processor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.Path;
import org.aksw.jena_sparql_api.concepts.Step;
import org.aksw.jena_sparql_api.geo.GeoMapSupplierUtils;
import org.aksw.jena_sparql_api.utils.ElementTreeAnalyser;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitor;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.update.Update;

class ModelUtils {
    
    public static Update deleteByConcept(Concept concept) {
        return null;
    }

    public static void clearGeometryWgs(Model model) {
        
        Concept c = GeoMapSupplierUtils.conceptWgs84;

        String wgs = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> DELETE WHERE { ?s geo:long ?x ; geo:lat ?y }";
        String wgsGeometry = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> DELETE WHERE { ?s geo:geometry ?y }";
        String geoSparqlLiteral = "PREFIX sp: <http://www.opengis.net/ont/geosparql#> DELETE WHERE { ?s sp:asWKT ?w }";
        // TODO Also add a delete statement that removes geometry resources (so not just the literal)???
    }
    
    
    
    /**
     * Find the set of resources referenced by the path
     */
    public static Set<RDFNode> getNodes(Model model, RDFNode first, Path path) {
        List<Step> steps = path.getSteps();
        
        Set<RDFNode> starts = Collections.singleton(first);
        
        
        for(Step step : steps) {
            String propertyName = step.getPropertyName();
            boolean isInverse = step.isInverse();

            Property property = model.createProperty(propertyName);

            
            Set<RDFNode> nodes = new HashSet<RDFNode>();
            
            for(RDFNode start : starts) {
                Set<RDFNode> tmp;
                if(!isInverse) {
                    tmp = model.listObjectsOfProperty(start.asResource(), property).toSet();
                } else if(start.isResource()) {
                    tmp =  new HashSet<RDFNode>(model.listSubjectsWithProperty(property, start).toSet());
                } else {
                    tmp = Collections.<RDFNode>emptySet();
                }
                
                nodes.addAll(tmp);
            }
            starts = nodes;
        }
        
        return starts;
    }
}

public class PathSpec {
    private Path path;
}
