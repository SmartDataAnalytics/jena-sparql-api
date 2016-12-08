package org.aksw.jena_sparql_api.cache.tests;

import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

import com.google.common.collect.Range;

/**
 * The main question is still how to represent children of thi  tree structure.
 * Options:
 * (a) Every tree level node has a list of children and a field for the operator (union, join. etc)
 * (b) We create an OpQueryLevel node - hence, union, join and left join nodes remain as usual
 * (c) We create a custom class for sub-ops which depending on the operator type holds different attributes
 *
 *  i suppose option (b) is the most advantageous one
 *
 *
 *
 *
 * @author raven
 *
 */


/**
 * Make a bottom op traversal and generate the algebra
 * @author raven
 *
 */
class QueryLevelToOp {

    public Op process(QueryLevel ql) {
        //Op = process(ql.getSubgetSubLevel());
        //Op 

        Op result = null;

        for(Table table : ql.getTables()) {
            OpTable op = OpTable.create(table);
            result = OpJoin.create(result, op);
        }

        for(Expr expr : ql.getFilters()) {
            result = OpFilter.filter(expr, result);
        }

        if(ql.getQuadPattern() != null) {
            result = new OpQuadBlock(ql.getQuadPattern());
        }

        return result;
    }

    public static void applySolutionModifiers(SolutionModifiers sm, Op op) {
// https://www.w3.org/TR/sparql11-query/#convertGroupAggSelectExpressions
//        Grouping
//        Aggregates
//        HAVING
//        VALUES
//        Select expressions

        Op result = op;

        if(sm.getProjection() != null) {
            result = new OpProject(result, sm.getProjection());
        }

        if(sm.getExtend() != null) {
            result = OpExtend.create(op, sm.getExtend());
        }

        if(sm.getSlice() != null) {
            Range<Long> range = sm.getSlice();
            long lower = range.lowerEndpoint();
            long upper = range.upperEndpoint();
            long length = upper - lower;

            result = new OpSlice(result, lower, length);
        }


        if(sm.getSortConditions() != null) {
            result = new OpOrder(result, sm.getSortConditions());
        }

        int dedupLevel = sm.getDeduplicationLevel();
        switch(dedupLevel) {
        case 0: /* nothing todo */
        case 1: result = OpReduced.create(result); break;
        case 2: result = OpDistinct.create(result); break;
        default: throw new RuntimeException("Invalid deduplication level");
        }
    }


    //public QueryLevel
}





class QueryLevel {
    protected SolutionModifiers solutionModifiers;

    protected VarExprList groupExprs;
    protected List<ExprAggregator> aggregators;

    // TODO how to get the canonical quad filter pattern here?
    // Probably it would be best to have a map which maps the query level to the
    // canonical quad filter pattern

    protected ExprList filters;
    protected QuadPattern quadPattern;
    protected List<Table> tables;

    public SolutionModifiers getSolutionModifiers() {
        return solutionModifiers;
    }

    public void setSolutionModifiers(SolutionModifiers solutionModifiers) {
        this.solutionModifiers = solutionModifiers;
    }

    public VarExprList getGroupExprs() {
        return groupExprs;
    }

    public void setGroupVars(VarExprList groupExprs) {
        this.groupExprs = groupExprs;
    }

    public List<ExprAggregator> getAggregators() {
        return aggregators;
    }

    public void setAggregators(List<ExprAggregator> aggregators) {
        this.aggregators = aggregators;
    }

    public ExprList getFilters() {
        return filters;
    }

    public void setFilters(ExprList filters) {
        this.filters = filters;
    }

    public QuadPattern getQuadPattern() {
        return quadPattern;
    }

    public void setQuadPattern(QuadPattern quadPattern) {
        this.quadPattern = quadPattern;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    
    @Override
    public String toString() {
        return "QueryLevel [solutionModifiers=" + solutionModifiers + ", groupExprs=" + groupExprs + ", aggregators="
                + aggregators + ", filters=" + filters + ", quadPattern=" + quadPattern + ", tables=" + tables + "]";
    }
}




//class OpQueryLevel {
//    protected QueryLevel queryLevel;
//    protected Op subOp; // may be null
//}
public class OpQueryLevel
    extends Op1
{
    protected QueryLevel data;

    // Probably we should remove the implied data attribute, and use a map
    // to associate a query level which the additional information
    protected QueryLevel impliedData;

    public OpQueryLevel(Op subOp) {
        super(subOp);
    }




//    public Op getSubLevel() {
//        return subOp;
//    }

    public QueryLevel getData() {
        return data;
    }




    public void setData(QueryLevel data) {
        this.data = data;
    }




    public QueryLevel getImpliedData() {
        return impliedData;
    }




    public void setImpliedData(QueryLevel impliedData) {
        this.impliedData = impliedData;
    }




    @Override
    public void visit(OpVisitor opVisitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op apply(Transform transform, Op subOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Op1 copy(Op subOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        out.println("( QueryLevel:");
        out.incIndent();

        SolutionModifiers sm = data.getSolutionModifiers();
        
        out.println("DeduplicationLevel: " + sm.getDeduplicationLevel());
        out.println("Projection: " + sm.getProjection());
        out.println("Extend: " + sm.getExtend());
        out.println("Slice: " + sm.getSlice());
        out.println("Sort Conditions: " + sm.getSortConditions());
        out.println("Aggregators: " + data.getAggregators());
        out.println("Group Exprs: " + data.getGroupExprs());
        out.println("Filters: " + data.getFilters());
        out.println("QuadPattern: " + data.getQuadPattern());
        out.println("SubOp:");
//        data.get
        
        
        out.decIndent();
        out.println(")");

        // TODO Auto-generated method stub
        super.output(out, sCxt);
    }

//    return "QueryLevel [" + "impliedSolutionModifiers=" + impliedSolutionModifiers
//            + ", solutionModifiers=" + solutionModifiers + ", filters=" + filters + ", quadPattern=" + quadPattern
//            + ", subOp=" + this.getSubOp() + "]";

}
