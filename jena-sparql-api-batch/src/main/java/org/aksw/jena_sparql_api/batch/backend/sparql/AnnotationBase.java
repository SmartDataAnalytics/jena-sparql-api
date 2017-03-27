package org.aksw.jena_sparql_api.batch.backend.sparql;

import java.lang.annotation.Annotation;

public abstract class AnnotationBase
    implements Annotation
{
    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}

