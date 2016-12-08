package org.aksw.jena_sparql_api.concept_cache.dirty;
//package org.aksw.jena_sparql_api.concept_cache;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.jena.sparql.algebra.Op;
//import org.apache.jena.sparql.algebra.OpVisitorBase;
//import org.apache.jena.sparql.algebra.op.OpDisjunction;
//import org.apache.jena.sparql.algebra.op.OpDistinct;
//import org.apache.jena.sparql.algebra.op.OpJoin;
//import org.apache.jena.sparql.algebra.op.OpProject;
//import org.apache.jena.sparql.algebra.op.OpQuadPattern;
//
//// Extras: min-height tagger: tag each node of a query with its minimum height, so
//// that cache parts smaller than that are not considered.
//
//class Candidate
//{
//    private Op startCandOp; // Maybe this should be a key which can be used to refer to a candidate op?
//    private Op currentCandOp; // The current op
//
//    private Op startQueryOp; // The op node in the query where this candidate starts off
//
//    public Candidate(Op startQueryOp, Op startCandOp) {
//        this.startQueryOp = startQueryOp;
//        this.startCandOp = startCandOp;
//        this.currentCandOp = currentCandOp;
//    }
//
//    public Op getCurrentCandOp() {
//        return currentCandOp;
//    }
//
//
//}
//
//
//
///**
// * Traverse a query's AET and navigate
// *
// * @author raven
// *
// */
//public class OpVisitorCacheApplier
//    extends OpVisitorBase
//{
//    private List<Op> candidates;
//
//    public OpVisitorCacheApplier(List<Op> candidates) {
//        this.candidates = candidates;
//    }
//
//    public void addCandidatesStartingAtThisOp(Op queryOp) {
//        Class<?> clazz = op.getClass();
//
//        List<Op> ops = getOpsByType(candidates);
//        List<Candidate> candidates = new ArrayList<Candidate>();
//        for(Op op : ops) {
//            Candidate cand = new Candidate(queryOp, candOp);
//        }
//    }
//
//
//    /**
//     * Iterate the candidates and return those that match the structure
//     *
//     * @param queryOp
//     * @return
//     */
//    public static List<Op> getOpsByType(List<Op> candidates, Class<?> clazz) {
//        for(Op candidate : candidates) {
//            if(candidate.getClass().isAssignableFrom(clazz)) {
//
//            }
//        }
//    }
//
//    @Override
//    public void visit(OpQuadPattern op) {
//
//    }
//
//    @Override
//    public void visit(OpDistinct op) {
//        getCandidatesStartingAtThisOp(op);
//
//    }
//
//    @Override
//    public void visit(OpProject op) {
//        getCandidatesStartingAtThisOp(op);
//
//        op.getVars();
//
//        for(Op candidate : candidates) {
//            if(!(candidate instanceof OpProject)) {
//                continue;
//            }
//
//            OpProject opCand = (OpProject)candidate;
//
//
//            // Candidates must have equal or more vars than the query
//            // So it must be possible to map each query var to one of the candidate
//
//            int numCandVars = opCand.getVars().size();
//            int numQueryVars = op.getVars().size();
//
//            if(numCandVars < numQueryVars) {
//                continue;
//            }
//
//            // If both are 1, we can use this information to enforce a mapping
//            if(numCandVars == 1 && numQueryVars == 1) {
//                //Map<Var, Var> map;
//                // map.put()
//            }
//
//            // If the candidate survived, move on to its child
//            opCand.getSubOp();
//            op.getSubOp();
//
//        }
//    }
//
//    @Override
//    public void visit(OpDisjunction) {
//        //getCandidatesStartingAtThisOp(op);
//
//    }
//
//}
