package org.aksw.jena_sparql_api.cache.tests;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpDatasetNames;
import org.apache.jena.sparql.algebra.op.OpDiff;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLabel;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpList;
import org.apache.jena.sparql.algebra.op.OpMinus;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpPath;
import org.apache.jena.sparql.algebra.op.OpProcedure;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpPropFunc;
import org.apache.jena.sparql.algebra.op.OpQuad;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTopN;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;

import com.google.common.collect.Range;

/**
 * Perform a depth first traversal, where nodes are visited before descending
 * in order to create OpQueryLevel objects.
 * 
 * 
 * Outdated: Perform a depth first traversal. If a leaf is found, navigate to the parents and
 * collect the query level data. If an op is encountered that cannot be added to the current query level, create a new
 * one and it the prior level as a child of the new one.
 * If the parent is e.g. a join or a union, create the corresponding op and
 * attach all constructed query levels so far as children.
 *
 * @author raven
 *
 */
class OpToQueryLevel
    implements OpVisitor
{
    public static final int PRECEDENCE_QUADS_OR_FILTERS = 0;

    public static final int PRECEDENCE_PROJECT = 3;
    public static final int PRECEDENCE_AGGREGATE = 3;
    public static final int PRECEDENCE_GROUPBY = 3;
    public static final int PRECEDENCE_ORDERBY = 3;
    public static final int PRECEDENCE_DISTINCT = 4;
    public static final int PRECEDENCE_SLICE = 5;
    
    public static final int PRECEDENCE_NESTED = 1000;

    public static OpQueryLevel toQueryLevel(Op op) {
        OpToQueryLevel visitor = new OpToQueryLevel();
        op.visit(visitor);
        OpQueryLevel result = visitor.getResult();
        return result;
    }

    protected OpQueryLevel currentLevel;
    protected int currentPrecedence = 0;

    public OpQueryLevel getResult() {
        return currentLevel;
    }
    
    @Override
    public void visit(OpBGP opBGP) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpQuadPattern quadPattern) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpQuadBlock quadBlock) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpTriple opTriple) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpQuad opQuad) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpPath opPath) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpTable opTable) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpNull opNull) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpProcedure opProc) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpPropFunc opPropFunc) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpFilter opFilter) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpGraph opGraph) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpService opService) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpDatasetNames dsNames) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpLabel opLabel) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpAssign opAssign) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpExtend opExtend) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpJoin opJoin) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpUnion opUnion) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpDiff opDiff) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpMinus opMinus) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpConditional opCondition) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpSequence opSequence) {
        // TODO Auto-generated method stub

    }


    @Override
    public void visit(OpDisjunction opDisjunction) {
        //OpQueryLevel ql = ensure(PRECEDENCE_NESTED); 
        OpDisjunction parentOp = OpDisjunction.create();
        for(Op subOp : opDisjunction.getElements()) {
            OpToQueryLevel visitor = new OpToQueryLevel();
            subOp.visit(visitor);
            Op childOp = visitor.getResult();
            parentOp.add(childOp);
        }
        //ql.
        
        currentLevel = new OpQueryLevel(parentOp);
    }


    @Override
    public void visit(OpList opList) {
        throw new UnsupportedOperationException("not implemented yet");
    }


    @Override
    public void visit(OpOrder opOrder) {
        opOrder.getSubOp().visit(this);
        
        OpQueryLevel ql = ensure(PRECEDENCE_ORDERBY);
        ql.getData().getSolutionModifiers().setSortConditions(opOrder.getConditions());
    }

    public OpQueryLevel ensure(int precedenceLevel) {
        OpQueryLevel result = ensure(precedenceLevel, false);
        return result;
    }

    public OpQueryLevel ensure(int precedenceLevel, boolean acceptEqual) {
        boolean isAccepted = acceptEqual
                ? precedenceLevel <= currentPrecedence
                : precedenceLevel < currentPrecedence;

        OpQueryLevel result = isAccepted
            ? currentLevel
            : new OpQueryLevel(currentLevel);

        currentPrecedence = precedenceLevel;
        return result;
    }

//    public OpQueryLevel ensure(Op subOp, int precedenceLevel, boolean acceptEqual) {
//        boolean isAccepted = acceptEqual
//                ? precedenceLevel <= currentPrecedence
//                : precedenceLevel < currentPrecedence;
//
//        OpQueryLevel result = isAccepted
//            ? currentLevel
//            : new OpQueryLevel(subOp);
//
//        currentPrecedence = precedenceLevel;
//        return result;
//    }

    @Override
    public void visit(OpProject op) {
        op.getSubOp().visit(this);

        OpQueryLevel ql = ensure(PRECEDENCE_PROJECT);
        ql.getData().getSolutionModifiers().setProjection(op.getVars());
    }


    @Override
    public void visit(OpReduced op) {
        op.getSubOp().visit(this);

        OpQueryLevel ql = ensure(PRECEDENCE_DISTINCT);
        ql.getData().getSolutionModifiers().setDeduplicationLevel(1);
    }


    @Override
    public void visit(OpDistinct op) {
        op.getSubOp().visit(this);

        OpQueryLevel ql = ensure(PRECEDENCE_DISTINCT);
        ql.getData().getSolutionModifiers().setDeduplicationLevel(2);
    }


    @Override
    public void visit(OpSlice op) {
        op.getSubOp().visit(this);

        OpQueryLevel ql = ensure(PRECEDENCE_SLICE);
        long start = op.getStart();
        long end = start + op.getLength();
        ql.getData().getSolutionModifiers().setSlice(Range.closed(op.getStart(), end));
    }


    @Override
    public void visit(OpGroup op) {
        op.getSubOp().visit(this);

        OpQueryLevel ql = ensure(PRECEDENCE_GROUPBY);
        ql.getData().setAggregators(op.getAggregators());
        ql.getData().setGroupVars(op.getGroupVars());
    }


    @Override
    public void visit(OpTopN op) {
        throw new UnsupportedOperationException("not implemented yet");
    }
    
    
    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT DISTINCT ?s { { ?s a ?t } UNION { ?s a ?u } }");
        Op op = Algebra.toQuadForm(Algebra.compile(query));
        
        OpQueryLevel ql = OpToQueryLevel.toQueryLevel(op);
        System.out.println(ql);
    }
    

}