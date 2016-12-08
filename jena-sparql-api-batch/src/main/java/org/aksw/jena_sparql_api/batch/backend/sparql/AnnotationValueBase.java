package org.aksw.jena_sparql_api.batch.backend.sparql;

public abstract class AnnotationValueBase
    extends AnnotationBase
{
    protected String value;

    public AnnotationValueBase(String value) {
        super();
        this.value = value;
    }

    public String value() {
        return value;
    }
}
