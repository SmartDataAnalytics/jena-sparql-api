package org.aksw.jena_sparql_api.update;

public class FluentUpdateExecutionFactory {
    private UpdateExecutionFactory uef;

    public FluentUpdateExecutionFactory(UpdateExecutionFactory uef) {
        super();
        this.uef = uef;
    }

    public UpdateExecutionFactory create() {
        return uef;
    }


    public static FluentUpdateExecutionFactory from(UpdateExecutionFactory uef) {
        FluentUpdateExecutionFactory result = new FluentUpdateExecutionFactory(uef);
        return result;
    }
}
