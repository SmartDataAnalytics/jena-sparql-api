package org.aksw.jena_sparql_api.concept.builder.api;

/**
 * TODO What if we want to express mandatory attributes in the projection?
 * In that case, this actually translates to a restriction on the concept - i.e. exists(role, top)
 * So maybe the projection builder operates on top of a concept builder?
 *
 * Also, we want to express that whole sub-trees are optional -
 * the question is, whether this is done on a per-relation level, or per node level.
 *
 * Actually we want to express the modality of a certain property.
 *
 * @author raven
 *
 */
public interface TreeBuilder
{
    NodeBuilder getSource();
    NodeBuilder getPredicate();
    NodeBuilder getTarget();

    boolean isOptional();
    TreeBuilder setOptional(boolean b);
}
