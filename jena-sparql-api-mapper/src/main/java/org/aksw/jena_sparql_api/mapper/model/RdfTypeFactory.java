package org.aksw.jena_sparql_api.mapper.model;

import java.util.Collection;

import org.aksw.jena_sparql_api.shape.ResourceShapeBuilder;
import org.apache.jena.rdf.model.Resource;

public interface RdfTypeFactory {
    RdfType forJavaType(Class<?> clazz);
    
    
    /**
     * expose which properties to fetch in order to decide a node's corresponding
     * Java type(s)
     * 
     * @param rsb
     */
    // Probably these methods should not be part of the 'factory' but rather of a
    // TypeSystem (which makes use of the factry)
//    void exposeTypeDeciderShape(ResourceShapeBuilder rsb);
//    
//    Collection<RdfType> getApplicableTypes(Resource resource);
}
