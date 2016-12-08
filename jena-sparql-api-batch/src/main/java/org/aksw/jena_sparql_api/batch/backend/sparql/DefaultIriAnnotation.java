package org.aksw.jena_sparql_api.batch.backend.sparql;

import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;

public class DefaultIriAnnotation
    extends AnnotationValueBase
    implements DefaultIri
{
    public DefaultIriAnnotation(String value) {
        super(value);
    }
}
