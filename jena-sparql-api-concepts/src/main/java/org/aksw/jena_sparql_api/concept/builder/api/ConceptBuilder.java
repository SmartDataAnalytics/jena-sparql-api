package org.aksw.jena_sparql_api.concept.builder.api;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public interface ConceptBuilder
    extends Supplier<Concept>, Cloneable
{
    ConceptBuilder getParent();

    default ConceptBuilder getRoot() {
        ConceptBuilder parent = getParent();
        ConceptBuilder result = parent != null
                ? parent.getRoot()
                : null;

        return result;
    }

    ConceptBuilder clone();

//    public void addExists(Node node);
//    public void addForAll(Node node);

    /**
     * Sets an optional base concept on which further restrictions can be applied
     *
     * @param conceptBuilder
     * @return
     */
    ConceptBuilder setBaseConceptBuilder(ConceptBuilder conceptBuilder);


    /**
     * Retrieves the baseConceptBuilder.
     * May be null
     *
     * @return
     */
    ConceptBuilder getBaseConceptBuilder();


    RestrictionBuilder newRestriction();


    List<RestrictionBuilder> findRestrictions(Node node);


    ConceptBuilder addExpr(Expr expr);
    ConceptBuilder removeExpr(Expr expr);

    Set<Expr> getExprs();

    void setNegated(boolean status);

    void isNegated();



    /**
     *
     */

    <T> T accept(ConceptBuilderVisitor<T> visitor);
}
