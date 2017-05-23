package org.aksw.jena_sparql_api.views.index;

import org.aksw.commons.collections.trees.TreeNode;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.expr.Expr;

import com.google.common.collect.Multimap;

/**
 * Note: Actually its an index over a query's quad *filter* patterns
 * 
 * @author raven
 *
 */
public class QuadPatternIndex {
    /**
     * Index of an individual DNF clauses (i.e. this is not an index over the whole DNF)
     * Each key of the map corresponds to a blocking key, whereas an entry's set of values
     * is a subset of this clause's conjunction according to the blocking key
     */
    protected Multimap<Expr, Expr> groupedConjunction;
    
    /**
     * Reference to the node in the algebra expression
     */
    protected TreeNode<Op> opRef;
        
    /**
     * The opRef's corresponding qfpc
     */
    protected QuadFilterPatternCanonical qfpc;

    public QuadPatternIndex(Multimap<Expr, Expr> groupedConjunction,
            TreeNode<Op> opRef, QuadFilterPatternCanonical qfpc) {
        super();
        this.groupedConjunction = groupedConjunction;
        this.opRef = opRef;
        this.qfpc = qfpc;
    }

    public Multimap<Expr, Expr> getGroupedConjunction() {
        return groupedConjunction;
    }

    public TreeNode<Op> getOpRef() {
        return opRef;
    }

    public QuadFilterPatternCanonical getQfpc() {
        return qfpc;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupedConjunction == null) ? 0
                : groupedConjunction.hashCode());
        result = prime * result + ((opRef == null) ? 0 : opRef.hashCode());
        result = prime * result + ((qfpc == null) ? 0 : qfpc.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuadPatternIndex other = (QuadPatternIndex) obj;
        if (groupedConjunction == null) {
            if (other.groupedConjunction != null)
                return false;
        } else if (!groupedConjunction.equals(other.groupedConjunction))
            return false;
        if (opRef == null) {
            if (other.opRef != null)
                return false;
        } else if (!opRef.equals(other.opRef))
            return false;
        if (qfpc == null) {
            if (other.qfpc != null)
                return false;
        } else if (!qfpc.equals(other.qfpc))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "QuadPatternIndex [groupedConjunction=" + groupedConjunction
                + ", opRef=" + opRef + ", qfpc=" + qfpc + "]";
    }
}