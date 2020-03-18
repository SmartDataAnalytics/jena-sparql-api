package org.aksw.jena_sparql_api.algebra.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.trees.Tree;
import org.aksw.commons.collections.trees.TreeImpl;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsage;
import org.aksw.jena_sparql_api.algebra.analysis.VarUsageAnalyzerVisitor;
import org.aksw.jena_sparql_api.utils.CnfUtils;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.utils.QuadPatternUtils;
import org.aksw.jena_sparql_api.utils.VarExprListUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.apache.jena.ext.com.google.common.base.Objects;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.Op0;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.Op2;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpN;
import org.apache.jena.sparql.algebra.op.OpNull;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadBlock;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSequence;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class OpUtils {
    public static OpTable createEmptyTableUnionVars(Op ... subOps) {
    	List<Var> vars = Arrays.asList(subOps).stream()
    			.flatMap(op -> OpVars.visibleVars(op).stream())
    			// Exclude special purpose vars 
    			.filter(v -> !Var.isBlankNodeVar(v))
    			.distinct()
    			.collect(Collectors.toList());
    	
		TableData table = new TableData(vars, Collections.emptyList());
		OpTable result = OpTable.create(table);
		return result;
    }

    /**
     * Like OpVars.visibleVars, but filters out non-named vars Filter out
     * non-named vars
     *
     * @param op
     * @return
     */
    public static Set<Var> visibleNamedVars(Op op) {
        Set<Var> result = OpVars.visibleVars(op).stream().filter(x -> x.isNamedVar())
                .collect(Collectors.toCollection(Sets::newLinkedHashSet));
        return result;
    }

    public static List<Op> getUnionMembers(Op op) {
        List<Op> result;
        if (op instanceof OpUnion) {
            OpUnion o = (OpUnion) op;
            result = Arrays.asList(o.getLeft(), o.getRight());
        } else if (op instanceof OpDisjunction) {
            OpDisjunction o = (OpDisjunction) op;
            result = o.getElements();
        } else {
            result = Collections.singletonList(op);
        }
        return result;
    }

    /**
     * Low level function that simply turns a var-map into an project(extend())
     * expression.
     *
     * It is recommended to use wrapWithProjection which deals with variable
     * name clashes in the renaming.
     *
     * @param subOp
     * @param oldToNew
     * @return
     */
    public static Op extendWithVarMap(Op subOp, Map<Var, Var> oldToNew) {
    	// Remove identity mappings because they'd result in algebra expressions such as BIND(?x AS ?x)
    	// which breaks with Jena
    	Map<Var, Var> oldToNewWithoutIdentity = oldToNew.entrySet().stream()
    			.filter(e -> !Objects.equal(e.getKey(), e.getValue()))
    			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    	List<Var> projVars = oldToNew.values()
    			.stream()
    			.distinct()
    			.collect(Collectors.toList());

        VarExprList vel = VarExprListUtils.createFromVarMap(oldToNewWithoutIdentity);
        OpExtend opExtend = OpExtend.create(subOp, vel);
        OpProject result = new OpProject(opExtend, projVars);
        return result;
    }

    /**
     * safe version of extend with varMap - renames any colliding var names
     *
     * Select (?o As ?s) (?s As ?o) { ?s ?p ?o } ->
     *
     * Select (?o as ?v1) (?s As ?v2) { ?s ?p ?o } -> Select (?v1 As ?s) (?v2 As
     * ?o) { { Select (?o as ?v1) ... } }
     *
     * @param subOp
     * @param oldToNew
     * @return
     */
    public static Op wrapWithProjection(Op subOp, Map<Var, Var> oldToNew) {
        // Remove identity mappings
        //oldToNew = oldToNew.entrySet().stream().filter(e -> !Objects.equals(e.getKey(), e.getValue()))
        //        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (!oldToNew.isEmpty()) {

            // check which variables need to be renamed to fresh variables
            Set<Var> tgtVars = new HashSet<>(oldToNew.values());

            Set<Var> mapVars = oldToNew.keySet();
            // NOTE Usually oldToNew.keySet() should be a subset of the visible
            // vars, but
            // for robustness we just create the union
            Set<Var> srcVars = OpVars.visibleVars(subOp);
            srcVars.addAll(mapVars);

            Set<Var> collisions = Sets.intersection(tgtVars, srcVars);

            Set<Var> blacklist = Sets.union(tgtVars, srcVars);
            Generator<Var> gen = VarGeneratorBlacklist.create(blacklist);

            Map<Var, Var> newOldToNew = new LinkedHashMap<>();
            Map<Var, Var> conflictResolution = new LinkedHashMap<>();
            for (Entry<Var, Var> e : oldToNew.entrySet()) {
                Var v = e.getKey();
                Var w = e.getValue();
                boolean isConflict = !v.equals(w) && collisions.contains(w);

                // Non-conflict vars are not renamed - i.e. mapped to themselves
                Var t;
                if (isConflict) {
                    t = gen.next();
                    conflictResolution.put(v, t);
                } else {
                    t = v;
                    conflictResolution.put(v, v);
                }

                newOldToNew.put(t, w);
            }

            if (!newOldToNew.equals(oldToNew)) {
                subOp = extendWithVarMap(subOp, conflictResolution);
                oldToNew = newOldToNew;
            }

            subOp = extendWithVarMap(subOp, oldToNew);
        }
        return subOp;
    }


    /**
     * Apply project and extend as needed
     *
     * @param subOp
     * @param map
     * @return
     */
    public static Op applyExtendProject(Op result, Map<Var, Expr> map) {
        Set<Var> visibleVars = OpVars.visibleVars(result);

        Map<Var, Var> identityMappings = map.entrySet().stream()
                .filter(e -> e.getValue().isVariable() && e.getKey().equals(e.getValue().asVar()))
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().asVar()));

        Map<Var, Expr> extensions = map.entrySet().stream()
                .filter(e -> !identityMappings.containsKey(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if(!extensions.isEmpty()) {
            VarExprList vel = VarExprListUtils.createFromMap(extensions);
            result = OpExtend.extend(result, vel);
        }

        if(!visibleVars.equals(map.keySet())) {
            result = new OpProject(result, new ArrayList<>(map.keySet()));
        }

        return result;
    }

    /**
     * Count the number of operators in an op expression TODO Add versions that
     * support a predicate on whether to descend into an op, such as SERVICE
     *
     * @param op
     * @return
     */
    public long size(Op op) {
        // TODO
        System.out.println(
                "OpUtils::size: This is a hack; it does not return the exact number of nodes in an op expression");
        return linearizePrefix(op).count();
    }

    public static Stream<Op> linearizePrefix(Op op) {
        Stream<Op> result = ExprUtils.linearizePrefix(op, Collections.singleton(null), OpUtils::getSubOps);
        return result;
    }

    public static Generator<Var> freshVars(Op op) {
        Collection<Var> blacklistVars = OpVars.mentionedVars(op);

        Generator<Var> gen = VarGeneratorImpl2.create("v");
        Generator<Var> result = new VarGeneratorBlacklist(gen, blacklistVars);

        return result;
    }

    /**
     * Perform a top-down substitution
     *
     * @param op
     * @param descendIntoSubst
     * @param opToSubst
     * @return
     */
    public static Op substitute(Op op, boolean descendIntoSubst, Function<? super Op, ? extends Op> transformFn) {
        Op result = TreeUtils.substitute(op, descendIntoSubst, OpTreeOps.get(), transformFn);
        return result;
    }

    /**
     * In the expression op, find the node searchNode and replace it with
     * newNode
     *
     * @param op
     * @param descendIntoSubst
     * @param searchNode
     * @return
     */
    public static Op substitute(Op op, Op searchNode, Op newNode) {
        Op result = TreeUtils.substitute(op, false, OpTreeOps.get(), o -> o == searchNode ? newNode : null);
        return result;
    }

    // public static Op substitute(Op op, boolean descendIntoSubst, Function<?
    // super Op, ? extends Op> opToSubst) {
    // Op tmp = opToSubst.apply(op);
    //
    // // Descend into op if tmp was null (assigned in statement after this)
    // // or descend into the replacement op
    // boolean descend = tmp == null || descendIntoSubst;
    //
    // // Use op if tmp is null
    // tmp = tmp == null ? op : tmp;
    //
    // Op result;
    // if(descend) {
    // List<Op> newSubOps = OpUtils.getSubOps(tmp).stream()
    // .map(subOp -> substitute(subOp, descendIntoSubst, opToSubst))
    // .collect(Collectors.toList());
    //
    // result = OpUtils.copy(op, newSubOps);
    // } else {
    // result = tmp;
    // }
    //
    // return result;
    // }
    //
    // @SuppressWarnings("unchecked")
    // public static <K, V> void increment(Map<K, V> map, K key) {
    // V val = map.get(key);
    // Integer v;
    // if(val == null) {
    // v = 1;
    // } else {
    // v = ((Number)val).intValue() + 1;
    // }
    // map.put(key, (V)v);
    // }
    //
//	public static void summarize(Op op, Map<String, Object> result) {
//		String label = FunctionUtils.getClassLabel(op);
//		// result.put(label, true);
//		increment(result, label);
//
//		List<Op> subOps = OpUtils.getSubOps(op);
//		for (Op subOp : subOps) {
//			summarize(subOp, result);
//		}
//	}

    public static String getClassLabel(Object o) {
        String result = o == null ? "(null)" : o.getClass().getSimpleName();
        return result;
    }

    public static boolean isEquivalent(Op a, Op b) {

        boolean result = true;

        String cla = getClassLabel(a);
        String clb = getClassLabel(b);

        if (cla.equals(clb)) {
            List<Op> sas = getSubOps(a);
            List<Op> sbs = getSubOps(b);

            int n = sas.size();
            if (n == sbs.size()) {
                for (int i = 0; i < n; ++i) {
                    Op sa = sas.get(i);
                    Op sb = sbs.get(i);

                    boolean subResult = isEquivalent(sa, sb);
                    if (!subResult) {
                        result = false;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Traverse an op structure and create a map from each subOp to its
     * immediate parent
     *
     * NOTE It must be ensured that common sub expressions are different
     * objects, since we are using an identity hash map for mapping children to
     * parents
     *
     *
     * @param op
     * @return
     */
    public static Map<Op, Op> parentMap(Op rootOp) {
        Map<Op, Op> result = TreeUtils.parentMap(rootOp, OpUtils::getSubOps);
        return result;
    }

    public static Tree<Op> createTree(Op rootOp) {
        Tree<Op> result = TreeImpl.create(rootOp, OpUtils::getSubOps);
        return result;
    }

    // public static void parentMap(Op op, Map<Op, Op> result) {
    // List<Op> subOps = getSubOps(op);
    //
    // for(Op subOp : subOps) {
    // result.put(subOp, op);
    //
    // parentMap(subOp, result);
    // }
    // }

    public static Op copyOnChange(Op op, List<Op> subOps) {
        List<Op> origSubOps = getSubOps(op);

        Op result = origSubOps.equals(subOps) ? op : copy(op, subOps);

        return result;
    }

    public static Op copy(Op op, List<Op> subOps) {
        Op result;
        int l = subOps.size();
        if (op instanceof Op0) {
            //Assert.state(l == 0);
            Op0 o = (Op0) op;
            result = o.copy();
        } else if (op instanceof Op1) {
            //Assert.state(l == 1);
            Op1 o = (Op1) op;
            result = o.copy(subOps.get(0));
        } else if (op instanceof Op2) {
            //Assert.state(l == 2);
            Op2 o = (Op2) op;
            result = o.copy(subOps.get(0), subOps.get(1));
        } else if (op instanceof OpN) {
            OpN o = (OpN) op;
            result = o.copy(subOps);
        } else if (op instanceof OpCopyable) {
            OpCopyable o = (OpCopyable) op;
            result = o.copy(subOps);
        } else {
            throw new RuntimeException("Should not happen: Could not copy: " + op);
        }

        return result;
    }

    public static List<Op> getSubOps(Op op) {
        List<Op> result;

        if (op instanceof Op0) {
            result = Collections.emptyList();
        } else if (op instanceof Op1) {
            result = Collections.singletonList(((Op1) op).getSubOp());
        } else if (op instanceof Op2) {
            Op2 tmp = (Op2) op;
            result = Arrays.asList(tmp.getLeft(), tmp.getRight());
        } else if (op instanceof OpN) {
            result = ((OpN) op).getElements();
        } else if (op instanceof OpCopyable) {
            result = ((OpCopyable) op).getElements();
        } else if (op instanceof OpExt) {
            // TODO We probably should support descending into children of an
            // OpExt
            result = Collections.emptyList();
        } else {
            throw new RuntimeException("Should not happen: " + op);
        }

        return result;
    }

    /**
     * Traverses an op structure in order to determine whether it contains any
     * concrete triple or quad patterns. If no such pattern is found, the query
     * is independent of a dataset and hence it can be evaluated e.g. against an
     * empty in-memory model.
     *
     *
     * @param op
     * @return
     */
    public static boolean isPatternFree(Op op) {
        boolean isPattern = op instanceof OpQuadPattern || op instanceof OpQuadBlock || op instanceof OpTriple
                || op instanceof OpBGP;

        boolean result;

        if (isPattern) {
            result = false;
        } else {
            List<Op> subOps = getSubOps(op);

            result = true;
            for (Op subOp : subOps) {
                boolean tmp = isPatternFree(subOp);
                if (tmp == false) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    // Find all nodes satisfying a predicate
    public static Stream<Op> inOrderSearch(Op op, Predicate<Op> predicate) {
        Stream<Op> result = TreeUtils.<Op, Op>inOrderSearch(op, OpUtils::getSubOps, x -> x, (a, b) -> true)
                .map(e -> e.getKey()).filter(predicate);
        return result;
    }

    public static boolean isServiceFree(Op op) {
        boolean result = inOrderSearch(op, o -> !(o instanceof OpService)).count() == 0;
        return result;
    }

    /**
     * Maybe we need to change the method to is compatible(Tree<Op> tree, Op op,
     * Set<Var> vars, boolean distinct)
     *
     * Analyze a node of a tree for which of its variables are required for
     * evaluation in other portions of the query.
     *
     * Also keep track of which set of variables is affected by the first parent
     * distinct operation.
     *
     * Note: A projection does not imply reference of a variable: A variable is
     * only referenced if it is required for evaluating an expression or join,
     * or if it is part of the final result.
     *
     * Scoping: If we have Extend(?x := ?o * 2, { ?s ?p ?o }) Then a reference
     * of ?x implies a use of ?o
     *
     * So we can do bookkeeping with a multimap that maps each (possibly newly
     * introduced) variable to the set of referenced original variables
     *
     *
     * Goal: Given: View Query: Select Distinct ?s { ?s a Foo } User Query:
     * Select Distinct ?s { { ?s a Foo } UNION { ?s a Bar } } support injecting
     * the view query into the user query: Select Distinct ?s { viewRef(viewId,
     * {?s}) UNION { ?s a Bar } }
     *
     * Note: If the User Query was without Distinct, there would be no match
     *
     * Distinct ( Project(?s ?o, Union ( BGP(?s a Foo), // As only ?s is
     * projected and ?o is unbound, the distinct ?s trivially satisfies distinct
     * ?s ?o BGP(?s ?p ?o), ) ) )
     *
     * Variables that are used in aggregate expressions must not be distinct -
     * unless there is an appropriate distinct sub op
     *
     * We could have: Select (?s as ?o) (?o As ?s) { ?s ?p ?o } (virtuoso 7.2
     * accepts this, but gets it wrong) In this case we would get the
     * dependencies: { ?o -> { ?s }, ?s -> { ?o } }
     *
     * The thing we have to take care of is, that the extend node may 'kill'
     * prior variable definitions
     *
     * @param tree
     * @param current
     * @return
     */
    public static VarUsage analyzeVarUsage(Tree<Op> tree, Op current, VarUsageAnalyzerVisitor visitor) {

        Op parent;
        while ((parent = tree.getParent(current)) != null) {
            visitor.setCurrent(current);
            parent.visit(visitor);
            current = parent;
        }

        VarUsage result = visitor.getResult();

        return result;
    }

    public static VarUsage analyzeVarUsage(Tree<Op> tree, Op current) {
        VarUsageAnalyzerVisitor visitor = new VarUsageAnalyzerVisitor(tree, current);
        VarUsage result = analyzeVarUsage(tree, current, visitor);
        return result;
    }

    public static VarUsage analyzeVarUsage(Tree<Op> tree, Op current, Set<Var> availableVars) {
        VarUsageAnalyzerVisitor visitor = new VarUsageAnalyzerVisitor(tree, current, availableVars);
        VarUsage result = analyzeVarUsage(tree, current, visitor);
        return result;
    }

    public static Op toOp(QuadFilterPattern qfp) {
        List<Quad> quads = qfp.getQuads();
        ExprList exprs = new ExprList(qfp.getExpr());
        Op result = toOp(quads, OpQuadPattern::new);
        result = OpFilter.filterBy(exprs, result);
        return result;
    }

    public static Op toOp(QuadFilterPatternCanonical qfpc) {
        ExprList exprs = CnfUtils.toExprList(qfpc.getFilterCnf());
        Op result = toOp(qfpc.getQuads(), OpQuadPattern::new);
        result = OpFilter.filterBy(exprs, result);
        return result;
    }

    public static Op project(Op op, Iterable<Var> vars) {
        List<Var> varList = Lists.newArrayList(vars); // new
                                                        // ArrayList<Var>(vars);
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

        for (Entry<Node, BasicPattern> entry : map.entrySet()) {
            Op oqp = opFactory.apply(entry.getKey(), entry.getValue());// new
                                                                        // OpQuadPattern(entry.getKey(),
                                                                        // entry.getValue());
            opqs.add(oqp);
        }

        Op result;

        if (opqs.isEmpty()) {
            result = OpNull.create();
        } else if (opqs.size() == 1) {
            result = opqs.iterator().next();
        } else {
            OpSequence op = OpSequence.create();

            for (Op item : opqs) {
                op.add(item);
            }

            result = op;
        }

        return result;
    }

    /**
     *
     * Suggested arguments for opFactory: OpQuadPattern::new
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
