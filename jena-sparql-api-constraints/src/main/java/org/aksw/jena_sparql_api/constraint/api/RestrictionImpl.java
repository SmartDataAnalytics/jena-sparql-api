package org.aksw.jena_sparql_api.constraint.api;


import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.constraint.util.NodeRanges;
import org.aksw.jena_sparql_api.constraint.util.PrefixSet;
import org.aksw.jena_sparql_api.constraint.util.RdfTermType;
import org.aksw.jena_sparql_api.views.TernaryLogic;
import org.apache.jena.graph.Node;




/**
 * This class represents restrictions to be used on variables.
 *
 *
 * Rules
 * . Stating a constant value (node) must be consistent with at least one prefix (if there are any), or equivalent to a previous value.
 *   Additionally all prefixes are removed in that case.
 *
 * . If a restriction is inconsistent, retrieving fields is meaningless, as their values are not defined.
 *
 *
 * .) Methods return true if a change occurred
 *
 * . TODO More details
 *
 *
 * Further statements could be:
 *
 * statePattern()
 * stateRange(min, max)
 * stateDatatype()
 *
 * I really hope I am not ending up with my own Datalog+Constraints engine :/
 *
 *
 * TODO: Maybe the set of uriPrefixes should be replaced with a single prefix -
 * so that an instance of restriction really only states a single restriction.
 *
 * So my problem is how to deal with dis/conjunctions of restrictions efficiently
 *
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RestrictionImpl
    implements Constraint
{
    protected EnumSet<RdfTermType> termTypes;

    /** Only applicable if termTypes includes IRI */
    protected PrefixSet iriPrefixes;

    /** Valid ranges of values; only applicable for IRI and literal term types*/
    // Note NodeRanges itself cannot capture negations (e.g. ?x != <foo>)
    protected NodeRanges ranges;

    protected boolean isInconsistent = false;


    public RestrictionImpl clone() {
        return new RestrictionImpl(this);
    }

    public RestrictionImpl() {
        isInconsistent = Boolean.TRUE;
    }


    /** Create a restriction for the graph component; only allows for iris */
    public static RestrictionImpl forGraph() {
        return new RestrictionImpl(EnumSet.of(RdfTermType.IRI));
    }

    /** Create a restriction for the graph component; only allows for iris and bnodes */
    public static RestrictionImpl forSubject() {
        return new RestrictionImpl(EnumSet.of(RdfTermType.IRI, RdfTermType.BNODE));
    }

    /** Create a restriction for the graph component; only allows for iris */
    public static RestrictionImpl forPredicate() {
        return new RestrictionImpl(EnumSet.of(RdfTermType.IRI));
    }

    /** Create a restriction for the graph component; allows for iris, bnodes and literals */
    public static RestrictionImpl forObject() {
        return new RestrictionImpl(EnumSet.of(RdfTermType.IRI, RdfTermType.BNODE, RdfTermType.LITERAL));
    }


    /** Create a restriction for a given node */
    public static RestrictionImpl forNode(Node node) {
        RestrictionImpl result = forObject();
        result.stateNode(node);
        return result;
    }




    /**
     * Return true if 'this' is equal to or less restrictive than other
     *
     * @param other
     * @return
     */
    public boolean subsumesOrIsEqual(RestrictionImpl other) {
        boolean result = true;

        if (!termTypes.containsAll(other.termTypes)) {
            result = false;
        } else if (!this.ranges.subsumes(other.ranges)) {
            result = false;
        }

        if(result && iriPrefixes != null) {
            // Check of on of the prefixes is a prefix of the constant
            if(other.node != null) {
                if(this.uriPrefixes.containsPrefixOf(other.node.toString())) {
                    return true;
                }
            }

            if(other.getUriPrefixes() == null) {
                return false;
            } else {
                // Test whether each of this.prefixes is a prefix of other
                for(String prefix : other.getUriPrefixes().getSet()) {
                    if(!this.uriPrefixes.containsPrefixOf(prefix)) {
                        return false;
                    }
                }
            }
        }


        return true;
    }



    public RestrictionImpl(EnumSet<RdfTermType> termTypes) {
        this.stateTypes(termTypes);
    }


    public RestrictionImpl(RdfTermType type) {
        this.stateType(type);
    }

    public RestrictionImpl(PrefixSet prefixSet) {
        this.stateUriPrefixes(prefixSet);
    }

    public RestrictionImpl(Node node) {
        this.stateNode(node);
    }

    public RestrictionImpl(RestrictionImpl other) {
        this.type = other.type;
        this.node = other.node;
        if(other.uriPrefixes != null) {
            this.uriPrefixes = new PrefixSet(other.uriPrefixes);
        }
        this.isInconsistent = other.isInconsistent;
    }

    public boolean hasConstant() {
        return isConsistent() && node != null;
    }

    public boolean hasPrefix() {
        return !hasConstant() && uriPrefixes != null;
    }

    /**
     * Get the rdf term type of this restriction.
     * Deprecated because this can be a set.
     *
     * @return
     */
    @Deprecated
    public RdfTermType getType() {
        return type;
    }

    public EnumSet<RdfTermType> getRdfTermTypes() {
        EnumSet<RdfTermType> result = EnumSet.noneOf(RdfTermType.class);
        result.add(type);

        return result;
    }

    public Node getNode() {
        return node;
    }

    public PrefixSet getUriPrefixes() {
        return uriPrefixes;
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.restriction.IRestriction#stateRestriction(org.aksw.sparqlify.restriction.Restriction)
     */
    @Override
    public boolean stateRestriction(RestrictionImpl that) {
        RestrictionImpl other = (RestrictionImpl)that;

        if(other.isInconsistent == Boolean.TRUE) {
            return false;
        }

        isInconsistent = TernaryLogic.and(this.isInconsistent, other.isInconsistent);
        //isConsistent = this.isConsistent != false && other.isConsistent != false;

        if(isInconsistent == Boolean.FALSE) {
            return false;
        } else if(other.node != null) {
            return stateNode(other.node);
        } else if(other.uriPrefixes != null) {
            return stateUriPrefixes(other.uriPrefixes);
        } else if(other.getType() != RdfTermType.UNKNOWN) {
            return stateType(other.type);
        }

        throw new RuntimeException("Should not happen");
    }


    /* (non-Javadoc)
     * @see org.aksw.sparqlify.restriction.IRestriction#stateType(org.aksw.sparqlify.restriction.Type)
     */
    @Override
    public boolean stateType(RdfTermType newType) {
        if(isInconsistent == Boolean.FALSE) {
            return false;
        }

        if(type == RdfTermType.UNKNOWN) {
            if(newType != RdfTermType.UNKNOWN) {
                type = newType;
                isInconsistent = null;
                return true;
            }
            return false;
        } else {
            if(type.equals(newType)) {
                return false;
            } else {
                isInconsistent = Boolean.FALSE;
                return true;
            }
        }
    }

    public boolean stateTypes(EnumSet<RdfTermType> rdfTermTypes) {
        if (isInconsistent) {
            return false;
        }

        termTypes.retainAll(rdfTermTypes);

        // isInconsistent =
    }


    /* (non-Javadoc)
     * @see org.aksw.sparqlify.restriction.IRestriction#stateNode(org.apache.jena.graph.Node)
     */
    @Override
    public boolean stateNode(Node newNode) {
        boolean change = stateType(getNodeType(newNode));

        if(isInconsistent == Boolean.FALSE) {
            return change;
        }

        if(node == null) {
            if(uriPrefixes != null) {
                /*
                if(!node.isURI()) {
                    satisfiability = Boolean.FALSE;
                    return true;
                }*/

                if(!uriPrefixes.containsPrefixOf(newNode.getURI())) {
                    isInconsistent = Boolean.FALSE;
                    return true;
                }
            }

            node = newNode;
            isInconsistent = null;

            return true;

        } else {

            if(!node.equals(newNode)) {
                isInconsistent = Boolean.FALSE;
                return true;
            }

            return false;
        }
    }


    /* (non-Javadoc)
     * @see org.aksw.sparqlify.restriction.IRestriction#stateUriPrefixes(org.aksw.sparqlify.config.lang.PrefixSet)
     */
    @Override
    public boolean stateUriPrefixes(PrefixSet prefixes) {
        if(prefixes.isEmpty()) {
            throw new RuntimeException("Should not happen");
        }

        boolean change = stateType(RdfTermType.IRI);

        if(isInconsistent == Boolean.FALSE) {
            return change;
        }


        if(node != null) {
            if(!node.isURI() || !prefixes.containsPrefixOf(node.getURI())) {
                isInconsistent = Boolean.FALSE;

                return true;
            }

            // We have a constant, no need to track the prefixes
            return false;
        }

        // If no prefixes have been stated yet, state them
        if(uriPrefixes == null) {
            uriPrefixes = new PrefixSet();
            for(String s : prefixes.getSet()) {
                Set<String> ps = uriPrefixes.getPrefixesOf(s);
                uriPrefixes.removeAll(ps);
                uriPrefixes.add(s);
            }

            isInconsistent = uriPrefixes.isEmpty() ? false : null;
            return true;
        } else if(prefixes.isEmpty()) {

            // If we get here, then we were not inconsistent yet
            // TODO Not sure if the satisfiability computation also works for TRUE
            if(uriPrefixes.isEmpty()) {
                isInconsistent = Boolean.FALSE;
                return true;
            } else {
                return false;
            }
        }

        // {http:, mailto:addr} {http://foo, mailto:}

        // Note: If we have prefixes Foo and FooBar, we keep FooBar, which is more restrictive.
        for(String s : prefixes.getSet()) {
            Set<String> ps = uriPrefixes.getPrefixesOf(s, false);
            if(!ps.isEmpty()) {
                uriPrefixes.removeAll(ps);
                uriPrefixes.add(s);
            }
        }

        // Remove all entries that do not have a prefix in the other set
        Iterator<String> it = uriPrefixes.getSet().iterator();
        while(it.hasNext()) {
            String s = it.next();
            Set<String> ps = prefixes.getPrefixesOf(s);
            if(ps.isEmpty()) {
                it.remove();
            }
        }

        if(uriPrefixes.isEmpty()) {
            isInconsistent = Boolean.FALSE;
            return true;
        }

        // TODO Could sometimes return false
        return true;
        //return change;
    }



    @Override
    public boolean isInconsistent() {
        return isInconsistent;
    }




//  // If no prefixes have been stated yet, state them
//  if(uriPrefixes == null) {
//      uriPrefixes = new PrefixSet();
//      for(String s : prefixes.getSet()) {
//          Set<String> ps = uriPrefixes.getPrefixesOf(s);
//          uriPrefixes.removeAll(ps);
//          uriPrefixes.add(s);
//      }
//
//      isInconsistent = uriPrefixes.isEmpty() ? false : null;
//      return true;
//  } else if(prefixes.isEmpty()) {
//
//      // If we get here, then we were not inconsistent yet
//      // TODO Not sure if the satisfiability computation also works for TRUE
//      if(uriPrefixes.isEmpty()) {
//          isInconsistent = Boolean.FALSE;
//          return true;
//      } else {
//          return false;
//      }
//  }


    // To be done.
    /* (non-Javadoc)
     * @see org.aksw.sparqlify.restriction.IRestriction#statePattern(com.karneim.util.collection.regex.PatternPro)
     */
//    @Override
//    public void statePattern(PatternPro pattern) {
//        // If there is a pattern already, make it the intersection with the new pattern
//
//        // If there is a node, check if it conforms to the pattern
//
//        // If there are prefixes, check if they conform to the pattern
//
//        throw new NotImplementedException();
//    }

}