package org.aksw.jena_sparql_api.mapper.hashid;

import org.apache.jena.rdf.model.RDFNode;

public interface RdfMappingModel {

    HashIdProcessor getClassDescriptor(Class<?> clazz);



    public static HashIdCxt computeHash(RDFNode rdfNode) {
        HashIdCxt cxt = new HashIdCxtImpl();

        return computeHash(rdfNode, cxt);
    }

    public static HashIdCxt computeHash(RDFNode rdfNode, HashIdCxt hashCxt) {
        RdfMappingModel mappingModel = RdfMappingModels.get();

        return computeHash(mappingModel, rdfNode, hashCxt);
    }

    public static HashIdCxt computeHash(RdfMappingModel mappingModel, RDFNode rdfNode, HashIdCxt hashCxt) {


        return hashCxt;
    }
}
