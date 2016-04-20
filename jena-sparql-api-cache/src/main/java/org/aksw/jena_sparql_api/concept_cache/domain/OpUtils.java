package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprList;

import com.google.common.collect.Lists;

public class OpUtils {

    public static Op toOp(QuadFilterPattern qfp) {
        List<Quad> quads = qfp.getQuads();
        ExprList exprs = new ExprList(qfp.getExpr());
        Op result = toOp(quads, exprs);
        return result;
    }


    public static Op toOp(QuadFilterPatternCanonical qfpc) {
        ExprList exprs = CnfUtils.toExprList(qfpc.getFilterCnf());
        Op result = toOp(qfpc.getQuads(), exprs);
        return result;
    }

    public static Op project(Op op, Iterable<Var> vars) {
        List<Var> varList = Lists.newArrayList(vars); //new ArrayList<Var>(vars);
        Op result = new OpProject(op, varList);
        return result;
    }

    public static Op toOp(ProjectedQuadFilterPattern pqfp) {
        QuadFilterPattern qfp = pqfp.getQuadFilterPattern();
        Op op = toOp(qfp);

        Op result = project(op, pqfp.getProjectVars());
        return result;
    }

    public static Op toOp(Iterable<Quad> quads, ExprList exprs) {
        Map<Node, BasicPattern> index = QuadPatternUtils.indexBasicPattern(quads);

        List<OpQuadPattern> opqs = new ArrayList<OpQuadPattern>();

        for(Entry<Node, BasicPattern> entry : index.entrySet()) {
            OpQuadPattern oqp = new OpQuadPattern(entry.getKey(), entry.getValue());
            opqs.add(oqp);
        }


        Op result;

        if(opqs.isEmpty()) {
            result = OpNull.create();
        } else if(opqs.size() == 1) {
            result = opqs.iterator().next();
        } else {
            OpSequence op = OpSequence.create();

            for(OpQuadPattern item : opqs) {
                op.add(item);
            }

            result = op;
        }

        if(!exprs.isEmpty()) {
            result = OpFilter.filter(exprs, result);
        }

        return result;
    }
}
