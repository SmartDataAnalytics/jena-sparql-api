package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jena_sparql_api.utils.TripleUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpBGP;
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

    // TODO The test method might need additional arguments in the future,
    // such as the occurrences (gspo) of the node
    protected BiFunction<Node, Integer, SubstitutionStrategy> testTransform;

    public TransformReplaceConstants(Generator<Var> generator) {
        this(generator, null);
    }

//    public TransformReplaceConstants(Generator<Var> generator, boolean omitDefaultGraphFilter, Predicate<Node> testTransform) {
//        this(generator, omitDefaultGraphFilter, testTransform, true);
//    }

    /**
     *
     * @param generator
     * @param injectFilter
     * @param testTransform
     * @param substitute If true, do not substitute default graphs in quads with variables
     */
    public TransformReplaceConstants(Generator<Var> generator, BiFunction<Node, Integer, SubstitutionStrategy> testTransform) {
        this.generator = generator;
        this.testTransform = testTransform; //testTransform == null ? x -> true : testTransform;
    }


    /**
     * Transform function based on a predicate that accepts a Node to test.
     * A return value of true maps to
     * SUSTITUTE_AND_FILTER and false to RETAIN.
     *
     * @param op
     * @param testTransform
     * @return
     */
    public static Op transform(Op op, Predicate<Node> testTransform) {
        return transform(op, (x, i) -> {
            boolean tmp = testTransform.test(x);
            SubstitutionStrategy r = tmp ? SubstitutionStrategy.SUSTITUTE_AND_FILTER : SubstitutionStrategy.RETAIN;
            return r;
        });
    }

    /**
     * Transform function that decides whether to replace a concrete node based on a BiFunction.
     * The BiFunction receives the node and and the index in the triple/quad and must return an appropriate
     * SubstitutionStrategy value.
     * Indexes: 0=s, 1=p, 2=o, 3=g
     * Note that the graph component has index 3 in order to allow for uniform handling of
     * triples and quads.
     *
     * @param op
     * @param testTransform
     * @return
     */
    public static Op transform(Op op, BiFunction<Node, Integer, SubstitutionStrategy> testTransform)
    {
        Collection<Var> mentionedVars = OpVars.mentionedVars(op);

        //Set<Var> oldVisibleVars = OpVars.visibleVars(op);

        Generator<Var> gen = VarGeneratorBlacklist.create("v", mentionedVars);

        Transform transform = new TransformReplaceConstants(gen, testTransform);
        Op result = Transformer.transform(transform, op);

        // Ensure the correct projection
//        Set<Var> newVisibleVars = OpVars.visibleVars(op);
//
//        if(!oldVisibleVars.equals(newVisibleVars)) {
//            result = new OpProject(result, new ArrayList<>(oldVisibleVars));
//        }


        return result;
    }

    public static Op transform(Op op, SubstitutionStrategy defaultGraphSubstitutionStrategy)
    {
        BiFunction<Node, Integer, SubstitutionStrategy> testTransform = (node, i) -> {
            // Graph component has index 3 (s=0, p=1, o=2, g=3)
            SubstitutionStrategy r = (i == 3 && Quad.isDefaultGraph(node))
                    ? defaultGraphSubstitutionStrategy
                    : SubstitutionStrategy.SUSTITUTE_AND_FILTER;

            return r;
        };

        Op result = transform(op, testTransform);
        return result;
    }

    public static Op transform(Op op) {
        return transform(op, SubstitutionStrategy.SUBSTITUTE);
    }


    public static Map<Node, Var> transform(Map<Node, Var> nodeToVar, Iterable<Node> inNodes, Generator<Var> generator) {
        //Node result;
        Map<Node, Var> result = new LinkedHashMap<>();

        for(Node node : inNodes) {
            Node n = transform(nodeToVar, node, generator, null, SubstitutionStrategy.SUSTITUTE_AND_FILTER);
            if(n.isVariable() && !n.equals(node)) {
                result.put(node, (Var)n);
            }
        }

        return result;
    }

    public static Node transform(Map<Node, Var> nodeToVar, Node node, Generator<Var> generator, ExprList filters, SubstitutionStrategy strategy) {
        Node result;

        if(node.isConcrete() && strategy.isSubstitute()) {
            Var var = nodeToVar.get(node);
            if(var == null) {
                var = generator.next();
                nodeToVar.put(node, var);

                // Use of the constant Quad.defaultGraphNodeGenerated in the graph position results in a free variable.
//                if(filters != null && (!omitDefaultGraphFilter || !(isNodeInGraphComponent && Quad.isDefaultGraph(node)))) {
                if(filters != null && strategy.isInjectFilter()) {
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

    @Override
    public Op transform(OpBGP opBGP) {
        Map<Node, Var> nodeToVar = new HashMap<>();
        ExprList filters = new ExprList();

        List<Triple> ts = opBGP.getPattern().getList();
        BasicPattern triples = transform(ts, nodeToVar, filters);
        Op result = new OpBGP(triples);

        if(!filters.isEmpty()) {
            result = OpFilter.filterBy(filters, result);
        }

        Set<Var> oldVisibleVars = OpVars.visibleVars(opBGP);
        Set<Var> newVisibleVars = OpVars.visibleVars(result);
        if(!oldVisibleVars.equals(newVisibleVars)) {
            result = new OpProject(result, new ArrayList<>(oldVisibleVars));
        }

        // Note: We need to add a projection here, so we do not suddely yield more variables than
        // in the original pattern - otherwise, we could break e.g. SELECT * { ... } queries.
//        result = new OpProject(result, vars);

        return result;
    }


    public BasicPattern transform(
            Collection<Triple> ts,
            Map<Node, Var> nodeToVar,
            ExprList filters) {

        BasicPattern triples = new BasicPattern();
        // TODO Mapping of nodes might be doable with jena transform
        List<Node> nodes = new ArrayList<Node>();
        for(Triple triple : ts) {


            List<Node> tmp = TripleUtils.tripleToList(triple);
            for(int i = 0; i < tmp.size(); ++i) {
                Node node = tmp.get(i);
                SubstitutionStrategy strategy = testTransform.apply(node, i);
                Node n = transform(nodeToVar, node, generator, filters, strategy);
                nodes.add(n);
            }

            Triple t = TripleUtils.listToTriple(nodes);

            triples.add(t);

            nodes.clear();
        }

        return triples;
    }

    public Op transform(OpQuadPattern op) {

        //List<Var> vars = new ArrayList<>(OpVars.visibleVars(op));
        Map<Node, Var> nodeToVar = new HashMap<>();
        ExprList filters = new ExprList();
        //BasicPattern triples = new BasicPattern();

        Node gn = op.getGraphNode();
        SubstitutionStrategy strategy = testTransform.apply(gn, 3);

        Node graphNode = transform(nodeToVar, gn, generator, filters, strategy);

//        boolean retainDefaultGraphNode = true;
//        Node graphNode = retainDefaultGraphNode && Quad.isDefaultGraph(gn)
//                ? Quad.defaultGraphNodeGenerated
//                : testTransform.test(gn)
//                    ? transform(nodeToVar, gn, true, generator, filters, omitDefaultGraphFilter)
//                    : gn;


        List<Triple> ts = op.getBasicPattern().getList();
        BasicPattern triples = transform(ts, nodeToVar, filters);


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
