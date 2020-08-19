package org.aksw.jena_sparql_api.io.pipe.process;

public interface PipeTransformChain {
    PipeTransformChain add(PipeTransform xform);
}
