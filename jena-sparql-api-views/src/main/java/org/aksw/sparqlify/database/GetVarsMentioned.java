package org.aksw.sparqlify.database;

import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.views.IViewDef;
import org.aksw.jena_sparql_api.views.OpViewInstanceJoin;
import org.aksw.jena_sparql_api.views.VarsMentioned;
import org.aksw.jena_sparql_api.views.ViewInstance;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.Var;

public class GetVarsMentioned {
    @SuppressWarnings("unchecked")
    public static Set<Var> getVarsMentioned(Op op) {
        return (Set<Var>)MultiMethod.invokeStatic(GetVarsMentioned.class, "_getVarsMentioned", op);
    }

//    @Deprecated
//    public static Set<Var> _getVarsMentioned(RdfViewInstance op) {
//        return new HashSet<Var>(op.getQueryToParentBinding().keySet());
//    }


//    public static Set<Var> _getVarsMentioned(OpMapping op) {
//        Set<Var> result = new HashSet<Var>(op.getMapping().getVarDefinition().getMap().keySet());
//
//        return result;
//    }


    public static <T extends IViewDef> Set<Var> _getVarsMentioned(OpViewInstanceJoin<T> op) {
        Set<Var> result = new HashSet<Var>();
        for(ViewInstance<T> vi : op.getJoin().getViewInstances()) {
            result.addAll(vi.getBinding().getQueryVars());
        }

        return result;
    }

    public static Set<Var> _getVarsMentioned(OpQuadPattern op) {
        return SetUtils.asSet(OpVars.mentionedVars(op));//QuadPatternUtils.getVarsMentioned((op.getPattern());
    }

    public static Set<Var> _getVarsMentioned(OpQuadBlock op) {
        //return QuadUtils.getVarsMentioned(op.getPattern());
        return SetUtils.asSet(OpVars.mentionedVars(op));
    }

    public static Set<Var> _getVarsMentioned(OpExtend op) {
        Set<Var> result = new HashSet<Var>();

        result.addAll(op.getVarExprList().getVars());
        result.addAll(getVarsMentioned(op.getSubOp()));

        return result;
    }

    public static Set<Var> _getVarsMentioned(OpExtFilterIndexed op) {
        return getVarsMentioned(op.effectiveOp());
    }

    public static Set<Var> _getVarsMentioned(VarsMentioned op) {
        Set<Var> result = op.varsMentioned();
        return result;
    }

    /*
    public static Set<Var> getVarsMentioned(OpUnion op) {
        Set<Var> tmp = getVarsMentioned(op.getLeft());
        tmp.addAll(getVarsMentioned(op.getRight()));

        return tmp;
    }*/

//    public static Set<Var> _getVarsMentioned(OpRdfViewPattern op) {
//        Set<Var> result = new HashSet<Var>();
//
//        for(RdfViewInstance item : op.getConjunction().getViewBindings()) {
//            EquiMap<Var, Node> equiMap = item.getBinding().getEquiMap();
//
//            result.addAll(equiMap.getEquivalences().asMap().keySet());
//            result.addAll(equiMap.getKeyToValue().keySet());
//
//            //result.addAll(item.getQueryToParentBinding().keySet());
//        }
//
//        return result;
//    }

    public static Set<Var> _getVarsMentioned(Op1 op) {
        return getVarsMentioned(op.getSubOp());
    }

    public static Set<Var> _getVarsMentioned(Op2 op) {
        Set<Var> tmp = getVarsMentioned(op.getLeft());
        tmp.addAll(getVarsMentioned(op.getRight()));

        return tmp;
    }

    public static Set<Var> _getVarsMentioned(OpN op) {
        Set<Var> result = new HashSet<Var>();
        for(Op item : op.getElements()) {
            result.addAll(getVarsMentioned(item));
        }

        return result;
    }

}