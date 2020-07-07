package org.aksw.jena_sparql_api.mapper.hashid;


import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class HashIdProcessor {
    protected HashIdRegistry registry;

    public void process(RDFNode node, HashIdCxt cxt) {
        Class<?> clz = node.getClass();
        if(Resource.class.isAssignableFrom(clz)) {

        }

        ClassDescriptor cd = registry.getClassDescriptor(clz);
        HashIdProcessor processor = cd.getHashIdProcessor();

        processor.process(node, cxt);
    }
}
