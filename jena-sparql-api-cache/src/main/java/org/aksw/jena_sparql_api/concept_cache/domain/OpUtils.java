package org.aksw.jena_sparql_api.concept_cache.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.sparql.ProjectedQuadFilterPattern;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
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
        Op result = toOp(quads, OpQuadPattern::new);
        result = OpFilter.filterBy(exprs,  result);
        return result;
    }


    public static Op toOp(QuadFilterPatternCanonical qfpc) {
        ExprList exprs = CnfUtils.toExprList(qfpc.getFilterCnf());
        Op result = toOp(qfpc.getQuads(), OpQuadPattern::new);
        result = OpFilter.filterBy(exprs,  result);
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

    public static Op toOpGraphTriples(Node graphNode, BasicPattern bgp) {
        Op result = new OpBGP(bgp);
        result = Quad.defaultGraphNodeGenerated.equals(graphNode) ? result : new OpGraph(graphNode, result);
        return result;
    }

    public static Op toOp(Map<Node, BasicPattern> map, BiFunction<Node, BasicPattern, Op> opFactory) {
        List<Op> opqs = new ArrayList<Op>();

        for(Entry<Node, BasicPattern> entry : map.entrySet()) {
            Op oqp = opFactory.apply(entry.getKey(), entry.getValue());//new OpQuadPattern(entry.getKey(), entry.getValue());
            opqs.add(oqp);
        }

        Op result;

        if(opqs.isEmpty()) {
            result = OpNull.create();
        } else if(opqs.size() == 1) {
            result = opqs.iterator().next();
        } else {
            OpSequence op = OpSequence.create();

            for(Op item : opqs) {
                op.add(item);
            }

            result = op;
        }

        return result;
    }

    /**
     *
     * Suggested arguments for opFactory:
     * OpQuadPattern::new
     * OpUtils::toOpGraphTriples
     *
     * @param quads
     * @param opFactory
     * @return
     */
    public static Op toOp(Iterable<Quad> quads, BiFunction<Node, BasicPattern, Op> opFactory) {
        Map<Node, BasicPattern> index = QuadPatternUtils.indexBasicPattern(quads);
        Op result = toOp(index, opFactory);
        return result;

    }
}
