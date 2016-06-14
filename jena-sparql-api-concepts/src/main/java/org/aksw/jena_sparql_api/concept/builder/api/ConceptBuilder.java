package org.aksw.jena_sparql_api.concept.builder.api;

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;

public interface ConceptBuilder
    //extends Cloneable
{
    RestrictionBuilder getParent();

    default ConceptBuilder getRoot() {
        RestrictionBuilder parent = getParent();
        ConceptBuilder result = parent != null
                ? parent.getParent().getRoot()
                : null;

        return result;
    }

    //@Override
    ConceptBuilder clone() throws CloneNotSupportedException;

//    public void addExists(Node node);
//    public void addForAll(Node node);

    /**
     * Sets an optional base concept on which further restrictions can be applied
     *
     * @param conceptBuilder
     * @return
     */
    ConceptBuilder setBaseConceptExpr(ConceptExpr conceptExpr);


    /**
     * Retrieves the baseConceptBuilder.
     * May be null
     *
     * @return
     */
    ConceptExpr getBaseConceptExpr();


    RestrictionBuilder newRestriction();


    List<RestrictionBuilder> findRestrictions(Node node);


    ConceptBuilder addExpr(Expr expr);
    ConceptBuilder removeExpr(Expr expr);

    Set<Expr> getExprs();

    ConceptBuilder setNegated(boolean status);

    void isNegated();
}
