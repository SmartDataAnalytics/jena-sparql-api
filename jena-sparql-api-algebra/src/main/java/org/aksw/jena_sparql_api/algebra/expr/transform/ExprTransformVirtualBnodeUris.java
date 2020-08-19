package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.transform.TransformExprToBasicPattern;
import org.aksw.jena_sparql_api.algebra.transform.TransformPullFiltersIfCanMergeBGPs;
import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.algebra.utils.FixpointIteration;
import org.aksw.jena_sparql_api.stmt.SparqlStmtMgr;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprLib;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.user.ExprTransformExpand;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//interface NameOrExpr {
//	String makeString(String arg);
//}
//
//class Name
//	implements NameOrExpr
//{
//	protected String fnName;
//
//	@Override
//	public String makeString(String arg) {
//		String result = fnName + "(" + arg + ")";
//	}
//
//}

//class BnodeRewriteConfig {
//	String vendorLabel;
//	//String bnodeLabelFnSymbol;
//	//UserDefinedFunctionDefinition
//
//	//Function<String, String> bnodeLabelFn;
//	//Function
//
//	public static final String ns = "http://www.aksw.org/bnode/fn/";
//
//	public static final String typeErrorFnUri = ns + "typeError";
//	public static final String encodeBnodeFnUri = ns + "encodeBnodeFnUri";
//	public static final String isBnodeUriFnUri = ns + "isBnodeUri";
//	public static final String decodeBnodeUriFnUri = ns + "decodeBnodeUri";
//
//	public void registerParseBid(String exprStr, String argVarName) {
//		add("tmp:parseBid", exprStr, Collections.singletonList(Var.alloc(argVarName));
//	}
//
//	public void registerUnparseBid(String exprStr, String argVarName) {
//		add("tmp:parseBid", exprStr, Collections.singletonList(Var.alloc(argVarName));
//	}
//
//	public void add(String uri, String expr, List<Var> args) {
//
//	}
//
//
//	public static void foobar() {
////		this.bnodeLabelFnSymbol = bnodeLabelFnSymbol;
////		this.bnodeLabelFnUri = ns + vendorLabel + "bnode";
////		this.forceBnodeUriFnUri = ns + vendorLabel + "/forceBnodeUri";
//
//		List<Var> x = Collections.singletonList(Vars.x);
//
//		UserDefinedFunctionFactory f = new UserDefinedFunctionFactory();
////		f.add(bnodeLabelFnUri, bnodeLabelFnSymbol + "(?x)", x);
//		//f.add(bnodeLabelFnUri, "<http://jena.apache.org/ARQ/function#bnode>(?x)", x);
//	//ARQ.enableBlankNodeResultLabels(false);
//	//ARQ.constantBNodeLabels
//
//		String bnodePrefix = "bnode://";
//		f.add(typeErrorFnUri, "ABS('')", Collections.emptyList());
//		//f.add(unparseBnodeIdFnUri, )
//		f.add(encodeBnodeFnUri, "URI(CONCAT('bnode://', <tmp:unparseBid>(?x)))", x);
//		f.add(isBnodeUriFnUri, "ISURI(?x) && STRSTARTS(STR(?x), '" + bnodePrefix + "')", x);
//		f.add(decodeBnodeUriFnUri, "IF(<" + isBnodeUriFnUri + ">(?x), <tmp:parseBid>(STRAFTER(STR(?x), '" + bnodePrefix + "')), <" + typeErrorFnUri + "()>)", x);
//
//		f.add(forceBnodeUriFnUri, "IF(isBlank(?x), <" + encodeBnodeFnUri + ">(" + bnodeLabelFnSymbol + "(?x)), ?x)", x);
//	}
//
//
//}

/**
 * Decode "blanknode URIs" - i.e. URIs that represent blank nodes, such as bnode://{blank-node-label}
 * This means
 * ?x = <bnode://foobar>
 * becomes
 * encode(?x) = decode(<bnode://foobar>)
 * encode(?x) = 'foobar'
 *
 *
 * Issue: How to deal with queries that test for blank nodes?
 * In principle, this rewrite virtually eleminates them, so maybe the most reasonable approach is
 * to simply rewrite isBlank to true.
 * in fact, the standard bnode function would also have to yield bnode uris instead
 *
 *
 * bnodeLabel(?x) := if ?x is a blank node, return its label as an xsd:string, type error otherwise
 *
 *
 * Auxiliary functions (defined in terms of the aforementioned and SPARQL standard functions)
 *
 * typeError() := abs("")
 * encodeBnodeUri(xsd:string ?x) := URI(CONCAT('bnode://', ?x))   create an URI from a blank node label
 * isBnodeUri(uri) := isURI(?x) && STRSTARTS(STR(?x), 'bnode://') true if uri represents a bnode
 * decodeBnodeUri(uri ?x) := IF(isBnodeURI(?x), STRAFTER(STR(?x), 'bnode://'), typeError()), extract the blank node label from a URI
 * forceBnodeUri(?x) -> IF(isBlank(?x), encodeBnodeURI(bnodeLabel(?x)), ?x)
 *
 *
 *
 * Transformations:
 * - Given ?x = const
 *
 * if(isBnodeURI(const)) {
 *   emit bnodeLabel(?x) = decodeBnodeURI(uri)
 * }
 *
 * @author raven
 *
 */
public class ExprTransformVirtualBnodeUris
    extends ExprTransformCopy
{

    //parseBid()
    //unparseBid()
    //bidOf(?x)


//	protected Function<? super Expr, ? extends Expr> bnodeLabelFn = null;
//	protected Function<? super Expr, ? extends Expr> decodeBnodeUri = null;
//	protected Function<? super Expr, ? extends Expr> isBnodeUriFn = null;

//	public static final UserDefinedFunctionFactory f = UserDefinedFunctionFactory.getFactory();

    // The bnodeLabelFnUri is vendor specific, others may depend on it
//	public String bnodeLabelFnUri;
//	public String bnodeLabelFnSymbol;
//	public transient String forceBnodeUriFnUri;

    public static final String ns = "http://ns.aksw.org/function/";

    // These function IRIs must be provided as macros
    public static final String bidOfFnIri = ns + "bidOf";
    public static final String decodeBnodeIriFnIri = ns + "decodeBnodeIri";
    public static final String isBnodeIriFnIri = ns + "isBnodeIri";
    public static final String forceBnodeIriFnIri = ns + "forceBnodeIri";

    protected Map<String, UserDefinedFunctionDefinition> macros;
    protected Map<String, Boolean> propertyFunctions;


    public ExprTransformVirtualBnodeUris(
            Map<String, UserDefinedFunctionDefinition> macros,
            Map<String, Boolean> propertyFunctions) {
        super();
        this.macros = macros;
        this.propertyFunctions = propertyFunctions;
    }

//	public void registerFunctions() throws ParseException {
//		List<Var> x = Collections.singletonList(Vars.x);
//
//		f.add(bnodeLabelFnUri, bnodeLabelFnSymbol + "(?x)", x);
//		//f.add(bnodeLabelFnUri, "<http://jena.apache.org/ARQ/function#bnode>(?x)", x);
////ARQ.enableBlankNodeResultLabels(false);
////ARQ.constantBNodeLabels
//
//		String bnodePrefix = "bnode://";
//		f.add(typeErrorFnUri, "ABS('')", Collections.emptyList());
//		//f.add(unparseBnodeIdFnUri, )
//		f.add(encodeBnodeFnUri, "URI(CONCAT('bnode://', ?x))", x);
//		f.add(isBnodeUriFnUri, "ISURI(?x) && STRSTARTS(STR(?x), '" + bnodePrefix + "')", x);
//		f.add(decodeBnodeUriFnUri, "IF(<" + isBnodeUriFnUri + ">(?x), STRAFTER(STR(?x), '" + bnodePrefix + "'), <" + typeErrorFnUri + "()>)", x);
//
//		f.add(forceBnodeUriFnUri, "IF(isBlank(?x), <" + encodeBnodeFnUri + ">(" + bnodeLabelFnSymbol + "(?x)), ?x)", x);
//	}

//	@Override
//	public Expr transform(ExprFunction1 func, Expr a) {
//		Expr result = a instanceof E_IsBlank
//				? NodeValue.TRUE
//				: super.transform(func, a);
//
//		return result;
//	}

//    public static ExprFunction2 safeTrySubst(ExprFunction2 func, Expr lhs, Expr b, boolean swapped) {
//        // We may get expr eval exceptions during constant folding.
//        // For example, graphdb uses 'xsd:long' for entity ids. An expression ?x = <bnode://bar>
//        // is yields an intermediary expression xsd:long("bar") which raises an exception
//
//        ExprFunction2 result;
//        try {
//            result = TrySubst(func, lhs, b, swapped);
//        } catch(ExprEvalException e) {
//            result = null;
//        }
//
//        return result;
//    }


    @Override
    public Expr transform(ExprFunction2 func, Expr a, Expr b) {

        ExprFunction2 result = null;

        if(!a.isConstant() && b.isConstant()) {
            result = trySubst(func, a, b, false);
            a = result.getArg1();
            b = result.getArg2();
        }

        if(a.isConstant() && !b.isConstant()) {
            result = trySubst(func, b, a, true);
        }

        if(result == null) {
            result = (ExprFunction2)super.transform(func, a, b);
        }

        return result;
    }

    public static <T extends ExprFunction2> T copy(T func, Expr a, Expr b, boolean swapped) {
        @SuppressWarnings("unchecked")
        T result = swapped ? (T)func.copy(b, a) : (T)func.copy(a, b);
        return result;
    }
//
//	public static Expr subst(Map<String, UserDefinedFunctionDefinition> macros, String udfUri, Expr ... args) {
//		UserDefinedFunctionDefinition udfd = macros.get(udfUri); //f.get(udfUri);
//		UserDefinedFunction fi = (UserDefinedFunction)udfd.newFunctionInstance();
//
//		//ExprUtils.eval
//		ExprList el = new ExprList(Arrays.asList(args));
//		fi.build(udfUri, el);
//		Expr expr = fi.getActualExpr();
//		return expr;
//	}

    public static Expr expandMacro(Map<String, UserDefinedFunctionDefinition> macros, String udfUri, Expr ... args) {
        Expr e = new E_Function(udfUri, new ExprList(Arrays.asList(args)));

        Expr result = expandMacro(macros, e);
        return result;
    }

    public static Expr expandMacro(Map<String, UserDefinedFunctionDefinition> macros, Expr e) {
        ExprTransform xform = new ExprTransformExpand(macros);
        e = FixpointIteration.apply(100, e, x -> ExprTransformer.transform(xform, x));
        e = FixpointIteration.apply(100, e, ExprLib::foldConstants);
        //e = ExprLib.foldConstants(e);

        return e;
    }

    public static NodeValue eval(Map<String, UserDefinedFunctionDefinition> macros, String udfUri, Expr ... args) {
        Expr expr = expandMacro(macros, udfUri, args);
        NodeValue result = ExprUtils.eval(expr);

        //org.apache.jena.sparql.function.Function fi = udfd.newFunctionInstance();


//		ExprList el = new ExprList(Arrays.asList(args));
//		//fi.build(udfUri, el);
//
//		// Taken from ExprUtils.eval
//		Context context = ARQ.getContext().copy() ;
//        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
//        FunctionEnv env = new ExecutionContext(context, null, null, null) ;
//
//		NodeValue result = fi.exec(BindingFactory.binding(), el, udfUri, env);
        return result;
    }

    // x = <bnode://foo> --> bidOf(?x) = decodeBnodeIri(<bnode://foo>)
    public ExprFunction2 trySubst(ExprFunction2 func, Expr lhs, Expr b, boolean swapped) {
        NodeValue rhs = b.getConstant();

        boolean isRhsBnodeUri = eval(macros, isBnodeIriFnIri, rhs).getBoolean();
        ExprFunction2 result;
        if(isRhsBnodeUri) {
            NodeValue bnodeLabel;
            try {
                bnodeLabel = eval(macros, decodeBnodeIriFnIri, rhs);
            } catch(ExprEvalException e) {
                // FIXME We should induce a type error here
                bnodeLabel = NodeValue.FALSE;
            }

            Expr x = macros.get(bidOfFnIri).getBaseExpr();


            Expr labelCondition = ExprTransformer.transform(new ExprTransformSubstitute(Vars.x, lhs), x);
            result = copy(func, labelCondition, bnodeLabel, swapped);
        } else {
            result = copy(func, lhs, b, swapped);
        }

        return result;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExprTransformVirtualBnodeUris.class);

    public Query rewrite(Query query) {
        Query result = QueryUtils.rewrite(query, op -> {
            Op a = TransformReplaceConstants.transform(op, x -> x.isURI() ? eval(macros, isBnodeIriFnIri, NodeValue.makeNode(x)).getBoolean() : false);
            // new ExprTransformVirtualBnodeUris()
            Op b = Transformer.transform(null, this, a);
            Op c = forceBnodeUris(b);//ExprTransformVirtualBnodeUris.forceBnodeUris(b);

            Op d = TransformExprToBasicPattern.transform(c, fn -> {
                String id = org.aksw.jena_sparql_api.utils.ExprUtils.getFunctionId(fn.getFunction());
                Boolean subjectAsOutput = propertyFunctions.get(id);
                Entry<String, Boolean> r = subjectAsOutput == null ? null : Maps.immutableEntry(id, subjectAsOutput);
//                //System.out.println(id);
//                if("str".equals(id)) {
//                    return Maps.immutableEntry("http://foo.bar/baz", false);
//                }
                return r;
            });

            Op e = FixpointIteration.apply(d, x -> {
                x = TransformPullFiltersIfCanMergeBGPs.transform(x);
                x = Transformer.transform(new TransformMergeBGPs(), x);
                // TODO Add a transformation that tidies up
                // sequences of OpProject and OpExten
//        		x = Transformer.transform(new TransformPro(), x);
                return x;
            });


            return e;
        });

        //System.out.println("Rewrote query\n" + query + " to\n" + result);
        logger.debug("Rewrote query\n" + query + " to\n" + result);
        return result;
    }


    public static ExprTransformVirtualBnodeUris createTransformFromUdfModel(Model model, Collection<String> activeProfiles) {
        Set<String> profiles = new HashSet<>(activeProfiles);
        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);

        // FIXME Load property functions from model
        Map<String, Boolean> propertyFunctions = Collections.singletonMap("http://www.ontotext.com/owlim/entity#id", false);
        ExprTransformVirtualBnodeUris result = new ExprTransformVirtualBnodeUris(map, propertyFunctions);

        return result;
    }

    public static void main(String[] args) {
        Expr input = ExprUtils.parse("?x = <bnode://foobar>");
//		Expr input = ExprUtils.parse("<bnode://foo> = <bnode://bar>");

        Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
        SparqlStmtMgr.execSparql(model, "udf-inferences.sparql");

//        Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/jena"));
      Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/graphdb"));
        ExprTransformVirtualBnodeUris xform = createTransformFromUdfModel(model, profiles);
//        Map<String, UserDefinedFunctionDefinition> map = UserDefinedFunctions.load(model, profiles);
//
//        // FIXME Load property functions from model
//        Map<String, Boolean> propertyFunctions = Collections.singletonMap("http://www.ontotext.com/owlim/entity#id", false);
//
//
//        ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(map, propertyFunctions);
        //Expr actual = ExprTransformer.transform(new ExprTransformBnodeDecode(), input);
        //System.out.println(actual);

//		Query query = QueryFactory.create("SELECT * { ?s a ?t . ?s ?p ?o }");
//		Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } { ?s <bnode://foo> ?t . ?s ?p ?o . FILTER(?p = <bnode://bar>)}");
        Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } { ?s <bnode://foo> ?t . ?s ?p ?o . FILTER(?p = <bnode://bar>)} ORDER BY ?s");
        Query actual = xform.rewrite(query);

        //		Op op = Algebra.compile(query);
//		op = forceBnodeUris(op);
//		Query actual = OpAsQuery.asQuery(op);
        System.out.println(actual);

    }


    public Op forceBnodeUris(Op op) {
        List<Var> visibleVars = new ArrayList<>(OpVars.visibleVars(op));
        Set<Var> forbiddenVars = new HashSet<>(OpVars.mentionedVars(op));

        // Rename all visible vars
        Map<Var, Var> map = visibleVars.stream()
                .collect(Collectors.toMap(
                        v -> v, v -> VarGeneratorBlacklist.create(v.getName(), forbiddenVars).next()));

        Op tmp = NodeTransformLib.transform(n -> n.isVariable() ? map.getOrDefault(n, (Var)n) : n, op);

        VarExprList vel = new VarExprList();
        for(Var v : visibleVars) {
            vel.add(v, expandMacro(macros, forceBnodeIriFnIri, new ExprVar(map.get(v))));
        }
        Op result = new OpProject(OpExtend.create(tmp, vel), visibleVars);


        return result;
    }

}

