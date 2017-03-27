package org.aksw.jena_sparql_api.batch.backend.sparql;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;

public class IriAnnotation
    extends AnnotationValueBase
    implements Iri
{
    public IriAnnotation(String value) {
        super(value);
    }
}
