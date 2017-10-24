package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.Generator;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;


public class TransformReplaceConstants
    extends TransformCopy
{

    protected Generator<Var> generator;
    protected boolean omitDefaultGraphFilter;

    public TransformReplaceConstants(Generator<Var> generator, boolean omitDefaultGraphFilter) {
        this.generator = generator;
        this.omitDefaultGraphFilter = omitDefaultGraphFilter;
    }


    public static Triple listToTriple(List<Node> nodes) {
        return new Triple(nodes.get(0), nodes.get(1), nodes.get(2));
    }

    public static List<Node> tripleToList(Triple triple)
    {
        List<Node> result = new ArrayList<Node>();
        result.add(triple.getSubject());
        result.add(triple.getPredicate());
        result.add(triple.getObject());

        return result;
    }



    public static Op transform(Op op)
    {
        Collection<Var> mentionedVars = OpVars.mentionedVars(op);

        Set<Var> oldVisibleVars = OpVars.visibleVars(op);

        Generator<Var> gen = VarGeneratorBlacklist.create("v", mentionedVars);

        Transform transform = new TransformReplaceConstants(gen, false);
        Op result = Transformer.transform(transform, op);

        // Ensure the correct projection
//        Set<Var> newVisibleVars = OpVars.visibleVars(op);
//
//        if(!oldVisibleVars.equals(newVisibleVars)) {
//            result = new OpProject(result, new ArrayList<>(oldVisibleVars));
//        }


        return result;
    }

    public static Node transform(Map<Node, Var> nodeToVar, Node node, boolean isGraphNode, Generator<Var> generator, ExprList filters, boolean omitDefaultGraphFilter) {
        Node result;

        if(node.isConcrete()) {
            Var var = nodeToVar.get(node);
            if(var == null) {
                var = generator.next();
                nodeToVar.put(node, var);

                // Use of the constant Quad.defaultGraphNodeGenerated in the graph position results in a free variable.
                if(!omitDefaultGraphFilter || !(isGraphNode && node.equals(Quad.defaultGraphNodeGenerated))) {
                    Expr condition = new E_Equals(new ExprVar(var), NodeValue.makeNode(node));
                    filters.add(condition);
                }
            }
            result = var;
        } else {
            result = node;
        }

        return result;


    }

    public Op transform(OpQuadPattern op) {

        //List<Var> vars = new ArrayList<>(OpVars.visibleVars(op));

        Map<Node, Var> nodeToVar = new HashMap<>();


        ExprList filters = new ExprList();

        BasicPattern triples = new BasicPattern();

        boolean retainDefaultGraphNode = true;
        Node graphNode = retainDefaultGraphNode && op.getGraphNode().equals(Quad.defaultGraphNodeGenerated)
                ? Quad.defaultGraphNodeGenerated
                : transform(nodeToVar, op.getGraphNode(), true, generator, filters, omitDefaultGraphFilter);


        // TODO Mapping of nodes might be doable with jena transform
        List<Node> nodes = new ArrayList<Node>();
        for(Triple triple : op.getBasicPattern().getList()) {


            for(Node node : tripleToList(triple)) {
                Node n = transform(nodeToVar, node, false, generator, filters, omitDefaultGraphFilter);
                nodes.add(n);
            }

            Triple t = listToTriple(nodes);

            triples.add(t);

            nodes.clear();
        }

        Op result = new OpQuadPattern(graphNode, triples);

        if(!filters.isEmpty()) {
            result = OpFilter.filterBy(filters, result);
        }

        Set<Var> oldVisibleVars = OpVars.visibleVars(op);
        Set<Var> newVisibleVars = OpVars.visibleVars(result);
        if(!oldVisibleVars.equals(newVisibleVars)) {
            result = new OpProject(result, new ArrayList<>(oldVisibleVars));
        }


        // Note: We need to add a projection here, so we do not suddely yield more variables than
        // in the original pattern - otherwise, we could break e.g. SELECT * { ... } queries.
//        result = new OpProject(result, vars);

        return result;
    }

//
//  public static Op _replace(OpQuadBlock op) {
//      throw new RuntimeException("Not implemented yet");
////          ExprList filters = new ExprList();
////
////
////          //BasicPattern triples = new BasicPattern();
////          QuadPattern quadPattern = new QuadPattern();
////
////          //Node rawGraphNode = op.getGraphNode();
////
//////          Node commonGraphNode = null;
//////          if(rawGraphNode.isConcrete()) {
//////              // If the graph node is a concrete value - except for the default graph,
//////              // replace it with a variable that is constrained to that value
//////              if(!rawGraphNode.equals(Quad.defaultGraphNodeGenerated)) {
//////                  commonGraphNode = transform(rawGraphNode, false, generator, filters);
//////              }
//////          }
//////          else {
//////              // If the graph node is a variable, use it.
//////              commonGraphNode = rawGraphNode;
//////          }
////
////
////          List<Node> nodes = new ArrayList<Node>(4);
////          for(Quad quad : op.getPattern()) {
////
////                Node graphNode;
////                if(commonGraphNode != null) {
////                    graphNode = commonGraphNode;
////                } else {
////                    graphNode = Var.alloc(generator.next());
////                }
////                nodes.add(graphNode);
////
////
////              for(Node node : tripleToList(triple)) {
////
////                  Node n = transform(node, generator, filters);
////                  nodes.add(n);
////              }
////
////              //Triple t = listToTriple(nodes);
////
////              //triples.add(t);
////              Quad q = QuadUtils.listToQuad(nodes);
////              quadPattern.add(q);
////              nodes.clear();
////          }
////
////          Op result = new OpQuadBlock(quadPattern);
////
////          if(!filters.isEmpty()) {
////              result = OpFilter.filter(filters, result);
////          }
////
////          return result;
//  }

}
