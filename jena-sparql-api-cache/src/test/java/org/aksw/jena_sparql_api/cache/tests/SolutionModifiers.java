package org.aksw.jena_sparql_api.cache.tests;

import java.util.List;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVars;

import com.google.common.collect.Range;

/**
 * Maybe we should use Op objects for the
 * query level just like QueryLevelDetails class.
 * This would allow to keep a reference to the originating op of a solution modifier / pattern
 *
 * So the goal is to replace sub-trees in the query's algebra.
 * Hence, maybe we do not need to keep references to the algebra, but
 * instead use a map
 * Map<Op, QueryLevel> which associates each op with a query level
 * object.
 *
 *
 *
 * @author raven
 *
 */
public class SolutionModifiers {
    protected int deduplicationLevel; // 0 = no, 1 = reduced, 2 = distinct
    protected List<Var> projection;
    protected VarExprList extend;
    protected Range<Long> slice;
    protected List<SortCondition> sortConditions;

    // TODO Strictly speaking, grouping / aggregation is not a solution modifier
//    protected VarExprList groupExprs;
//    protected List<ExprAggregator> aggregators;

    //protected List<Expr> groupBy;

    public int getDeduplicationLevel() {
        return deduplicationLevel;
    }

    public void setDeduplicationLevel(int deduplicationLevel) {
        this.deduplicationLevel = deduplicationLevel;
    }

    public List<Var> getProjection() {
        return projection;
    }

    public void setProjection(List<Var> projection) {
        this.projection = projection;
    }

    public Range<Long> getSlice() {
        return slice;
    }

    public void setSlice(Range<Long> slice) {
        this.slice = slice;
    }

    public List<SortCondition> getSortConditions() {
        return sortConditions;
    }

    public void setSortConditions(List<SortCondition> sortConditions) {
        this.sortConditions = sortConditions;
    }

    public VarExprList getExtend() {
        return extend;
    }

    public void setExtend(VarExprList extend) {
        this.extend = extend;
    }


//    public List<Expr> getGroupBy() {
//        return groupBy;
//    }
//
//    public void setGroupBy(List<Expr> groupBy) {
//        this.groupBy = groupBy;
//    }
}
