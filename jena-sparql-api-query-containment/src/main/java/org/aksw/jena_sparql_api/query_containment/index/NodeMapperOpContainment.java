package org.aksw.jena_sparql_api.query_containment.index;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.aksw.commons.graph.index.jena.transform.OpDistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.analysis.DistinctExtendFilter;
import org.aksw.jena_sparql_api.algebra.utils.OpExtConjunctiveQuery;
import org.aksw.jena_sparql_api.algebra.utils.OpUtils;
import org.aksw.jena_sparql_api.algebra.utils.QuadFilterPatternCanonical;
import org.aksw.jena_sparql_api.utils.NodeTransformRenameMap;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;

public class NodeMapperOpContainment
	implements NodeMapperOp
    //implements TriFunction<Op, Op, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching>, Entry<BiMap<Node, Node>, ResidualMatching>>
{
	protected OpContext viewContext;
	protected OpContext userContext;

	
	public NodeMapperOpContainment(OpContext viewContext, OpContext userContext) {
		this.viewContext = viewContext;
		this.userContext = userContext;
	}
	
    @Override
    public Entry<BiMap<Node, Node>, ResidualMatching> apply(Op viewOp, Op userOp, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching> tm) {
//        Class<?> viewOpClass = viewOp == null ? null : viewOp.getClass();
//        Class<?> userOpClass = userOp == null ? null : userOp.getClass();

        viewOp = OpUtils.substitute(viewOp, false, (op) -> tm.getNodeMappings().containsRow(op) ? OpNull.create() : op);
        userOp = OpUtils.substitute(userOp, false, (op) -> tm.getNodeMappings().containsColumn(op) ? OpNull.create() : op);

        Class<?> viewOpClass = viewOp.getClass();
        Class<?> userOpClass = userOp.getClass();

        //tm.getNodeMappings().get(viewOp, userOp);
        
        // Substitute the mapped nodes of viewOp and userOp
        
        
        
        Entry<BiMap<Node, Node>, ResidualMatching> result;
        if(!Objects.equals(viewOpClass, userOpClass)) {
            result = null;
        } else {
            Class<?> c = viewOpClass;

            if(OpExtConjunctiveQuery.class.isAssignableFrom(c)) {
                result = map((OpExtConjunctiveQuery)viewOp, (OpExtConjunctiveQuery)userOp, tm);
            } else if(OpDistinctExtendFilter.class.isAssignableFrom(c)) {
                result = map((OpDistinctExtendFilter)viewOp, (OpDistinctExtendFilter)userOp, tm);
            } else if(OpDisjunction.class.isAssignableFrom(c)) {
            	System.out.println("GOT A disjunction");
            	result = null;
            } else {
            	NodeTransform nodeTransform = new NodeTransformRenameMap(tm.getOverallMatching());
            	Op x = NodeTransformLib.transform(nodeTransform, viewOp);
            	
            	result = Objects.equals(x, userOp) ? new SimpleEntry<>(HashBiMap.create(), new ResidualMatching(Collections.singleton(Collections.emptySet()))) : null;            	
            }

        }

        // Check for match between the given trees
        //boolean isMatch = result != null;
        //System.out.println((isMatch ? "Match" : "No match") + " between\n" + viewOp + "and\n" + userOp);

        return result;
    }


    public Entry<BiMap<Node, Node>, ResidualMatching> map(OpExtConjunctiveQuery viewOp, OpExtConjunctiveQuery userOp, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching> tm) {
        QuadFilterPatternCanonical view = viewOp.getQfpc().getPattern(); //.applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
        QuadFilterPatternCanonical user = userOp.getQfpc().getPattern();

        QuadFilterPatternCanonical viewTransformed = viewOp.getQfpc().getPattern().applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
        
        Set<Quad> viewQuads = viewTransformed.getQuads();
        Set<Quad> userQuads = user.getQuads();
        
        Set<Quad> residualJoinQuads = Sets.difference(userQuads, viewQuads);
        Set<Quad> diffQuadsB = Sets.difference(viewQuads, userQuads);
        
        System.out.println("DIFF quads: " + residualJoinQuads);
        System.out.println("DIFF quads: " + diffQuadsB);
        
        
        System.out.println("VarUsage view: " + viewContext.getOpToVarUsage().get(viewOp));
        System.out.println("VarUsage user: " + userContext.getOpToVarUsage().get(userOp));
        
//        QuadFilterPatternCanonical residual = user.diff(view);

        
//        DistinctExtendFilter view = viewOp.getDef().applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
//        DistinctExtendFilter user = userOp.getDef();

        BiMap<Node, Node> baseIso = tm.getOverallMatching();
        
        //Set<Set<Expr>> viewCnf = view.getFilter().getCnf();
        //Set<Set<Expr>> userCnf = user.getFilter().getCnf();
        Expr viewExpr = view.getExprHolder().getExpr();
        Expr userExpr = user.getExprHolder().getExpr();
        
        Multimap<BiMap<Node, Node>, Set<Set<Expr>>> maps = ExpressionMapper.computeResidualExpressions(baseIso, viewExpr, userExpr);
        
        System.out.println("Residual Exprs: " + maps);

        
        Entry<BiMap<Node, Node>, ResidualMatching> result = null;
        
        if(!maps.isEmpty()) {
        	Entry<BiMap<Node, Node>, Set<Set<Expr>>> e = maps.entries().iterator().next();
        	
        	
        	ResidualMatching residualMatching = new ResidualMatching(e.getValue());
        	residualMatching.setResidualQuadJoins(residualJoinQuads);
        	
        	result = new SimpleEntry<>(e.getKey(), residualMatching);        	
        }
        
        //Op result = null;
//        
//        Op result = residual.isEmpty()
//                ? OpNull.create()
//                : null;

        //return new SimpleEntry<>(HashBiMap.create(), result);
        return result;
    }


    public Entry<BiMap<Node, Node>, ResidualMatching> map(OpDistinctExtendFilter viewOp, OpDistinctExtendFilter userOp, TreeMapping<Op, Op, BiMap<Node, Node>, ResidualMatching> tm) {
        DistinctExtendFilter view = viewOp.getDef().applyNodeTransform(new NodeTransformRenameMap(tm.getOverallMatching()));
        DistinctExtendFilter user = userOp.getDef();

        BiMap<Node, Node> baseIso = tm.getOverallMatching();
        
        //Set<Set<Expr>> viewCnf = view.getFilter().getCnf();
        //Set<Set<Expr>> userCnf = user.getFilter().getCnf();
        Expr viewExpr = view.getFilter().getExpr();
        Expr userExpr = user.getFilter().getExpr();
        
        Multimap<BiMap<Node, Node>, Set<Set<Expr>>> maps = ExpressionMapper.computeResidualExpressions(baseIso, viewExpr, userExpr);
        
        System.out.println("Residual Exprs: " + maps);
        // TODO How to create the diff properly?
        // If we had access to the underlying graphs of the ops, we could use the information to deal with symmetric expressions
        
        
        Entry<BiMap<Node, Node>, ResidualMatching> result = null;
        
        if(!maps.isEmpty()) {
        	Entry<BiMap<Node, Node>, Set<Set<Expr>>> e = maps.entries().iterator().next();
        	result = new SimpleEntry<>(e.getKey(), new ResidualMatching(e.getValue()));        	
        }
        
        return result;

        // TODO Extensions may yield additional matchings

//        Op result = view.equals(user)
//                ? OpNull.create()
//                : null;
//
//        return new SimpleEntry<>(HashBiMap.create(), result);
    }

}

