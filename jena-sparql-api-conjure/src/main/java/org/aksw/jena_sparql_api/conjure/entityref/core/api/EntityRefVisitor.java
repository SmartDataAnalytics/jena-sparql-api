package org.aksw.jena_sparql_api.conjure.entityref.core.api;

public interface EntityRefVisitor<T> {
    T visit(EntityRefIdentifier entityRef);
    T visit(EntityRefIri entityRef);
    T visit(EntityRefMaven entityRef);
}
