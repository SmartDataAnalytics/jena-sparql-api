package org.aksw.jena_sparql_api.constraint.api;


import java.util.EnumSet;

import org.aksw.jena_sparql_api.constraint.util.PrefixSet;
import org.aksw.jena_sparql_api.constraint.util.RdfTermType;
import org.apache.jena.graph.Node;


public interface Constraint
    extends Cloneable
{
    /*
     * Meta data methods.
     */
    /*
    boolean isSet();
    RestrictionSet asSet();
    */

    /*
     * Core methods
     */

    boolean stateConstraint(Constraint other);

    // boolean stateRestriction(Constraint other);

    /**
     * State whether the resource is a URI or a Literal
     *
     * @param type
     * @return
     */
    boolean stateType(RdfTermType newType);

    /**
     * Stating a node implies stating the type
     *
     * @param newNode
     * @return
     */
    boolean stateNode(Node newNode);

    /**
     * States a set of valid prefixes.
     *
     * Note: Stating an empty set implies that no URI can be used as a value.
     * If you do not want to constrain the prefixes, don't call this method.
     *
     * If the set of prefixes becomes empty after stating more prefixes,
     * the constraint becomes inconsistent.
     *
     * @param prefixes
     */
    boolean stateUriPrefixes(PrefixSet prefixes);


    /**
     * Return true when the constraint is known to always yield false
     *
     * @return
     */
    boolean isContradiction();


    Constraint clone();

    /**
     * State a URI regex pattern.
     * To be done.
     */
//    void statePattern(PatternPro pattern);

    /*
     * Retrieval
     *
     * Important: All retrieval methods must return complete sets, otherwise
     * it will result in missing information
     */

    boolean hasConstant();

    /**
     * The set of possible RDF term types this restriction may have.
     *
     * @return
     */
    EnumSet<RdfTermType> getRdfTermTypes();

    /**
     * Retrieve the node if it has been set.
     * Implies an RDF term type consistent with the node value.s
     *
     * @return The set node. Null if not set.
     */
    Node getNode();


    /**
     * Retrieve the set of possible URI prefixes.
     * Implies Type.URI
     *
     * @return
     */
    PrefixSet getUriPrefixes();
}
