package org.aksw.jena_sparql_api.conjure.resourcespec;

public interface ResourceSpecVisitor<T> {
	T visit(ResourceSpecUrl spec);
	T visit(ResourceSpecInline spec);
}
