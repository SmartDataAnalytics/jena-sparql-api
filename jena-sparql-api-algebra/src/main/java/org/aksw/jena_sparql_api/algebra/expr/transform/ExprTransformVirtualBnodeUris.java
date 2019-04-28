package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.algebra.transform.TransformReplaceConstants;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorBlacklist;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.function.user.UserDefinedFunction;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.function.user.UserDefinedFunctionFactory;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.util.ExprUtils;

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
	
//	protected Function<? super Expr, ? extends Expr> bnodeLabelFn = null;
//	protected Function<? super Expr, ? extends Expr> decodeBnodeUri = null;
//	protected Function<? super Expr, ? extends Expr> isBnodeUriFn = null;

	public static final UserDefinedFunctionFactory f = UserDefinedFunctionFactory.getFactory();

	public static final String ns = "http://www.example.org/fn/";
	
	public static String bnodeLabelFnUri = ns + "bnodeLabel";
	public static String typeErrorFnUri = ns + "typeError";
	public static String encodeBnodeFnUri = ns + "encodeBnodeFnUri";
	public static String isBnodeUriFnUri = ns + "isBnodeUri";
	public static String decodeBnodeUriFnUri = ns + "decodeBnodeUri";
	public static final String forceBnodeUriFnUri = ns + "forceBnodeUri";

	public ExprTransformVirtualBnodeUris() {
		try {
			registerFunctions();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}		
	}

	public void registerFunctions() throws ParseException {
		List<Var> x = Collections.singletonList(Vars.x);

		f.add(bnodeLabelFnUri, "<http://jena.apache.org/ARQ/function#bnode>(?x)", x);
//ARQ.enableBlankNodeResultLabels(false);
//ARQ.constantBNodeLabels

		String bnodePrefix = "bnode://";
		f.add(typeErrorFnUri, "ABS('')", Collections.emptyList());	
		f.add(encodeBnodeFnUri, "URI(CONCAT('bnode://', ?x))", x);
		f.add(isBnodeUriFnUri, "ISURI(?x) && STRSTARTS(STR(?x), '" + bnodePrefix + "')", x);
		f.add(decodeBnodeUriFnUri, "IF(<" + isBnodeUriFnUri + ">(?x), STRAFTER(STR(?x), '" + bnodePrefix + "'), <" + typeErrorFnUri + "()>)", x);
		f.add(forceBnodeUriFnUri, "IF(isBlank(?x), <" + encodeBnodeFnUri + ">(<" + bnodeLabelFnUri + ">(?x)), ?x)", x);
	}
	
//	@Override
//	public Expr transform(ExprFunction1 func, Expr a) {
//		Expr result = a instanceof E_IsBlank
//				? NodeValue.TRUE
//				: super.transform(func, a);
//
//		return result;
//	}
	
	@Override
	public Expr transform(ExprFunction2 func, Expr a, Expr b) {

		ExprFunction2 result = null;

		if(!a.isConstant() && b.isConstant()) {
			result = trySubst(func, a, b, false);
			a = result.getArg1();
			b = result.getArg2();
		}

		if(a.isConstant()&& !b.isConstant()) {
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

	public static Expr subst(String udfUri, Expr ... args) {
		UserDefinedFunctionDefinition udfd = f.get(udfUri);
		UserDefinedFunction fi = (UserDefinedFunction)udfd.newFunctionInstance();
		
		//ExprUtils.eval
		ExprList el = new ExprList(Arrays.asList(args));
		fi.build(udfUri, el);
		Expr expr = fi.getActualExpr();
		return expr;
	}
	
	public static NodeValue eval(String udfUri, Expr ... args) {
		UserDefinedFunctionDefinition udfd = f.get(udfUri);
		org.apache.jena.sparql.function.Function fi = udfd.newFunctionInstance();
		
		//ExprUtils.eval
		ExprList el = new ExprList(Arrays.asList(args));
		fi.build(udfUri, el);
		NodeValue result = fi.exec(BindingFactory.binding(), el, udfUri, FunctionEnvBase.createTest());
		return result;
	}

	public ExprFunction2 trySubst(ExprFunction2 func, Expr lhs, Expr b, boolean swapped) {
		NodeValue rhs = b.getConstant();
		
		boolean isRhsBnodeUri = eval(isBnodeUriFnUri, rhs).getBoolean();
		ExprFunction2 result;
		if(isRhsBnodeUri) {
			NodeValue rhsLabel = eval(decodeBnodeUriFnUri, rhs);
			Expr x = f.get(bnodeLabelFnUri).getBaseExpr();
			
			
			Expr aaLabel = ExprTransformer.transform(new ExprTransformSubstitute(Vars.x, lhs), x);
			result = copy(func, aaLabel, rhsLabel, swapped);		
		} else {
			result = copy(func, lhs, b, swapped);
		}
		
		return result;
	}
	
	public static Query rewrite(Query query) {
		Query result = QueryUtils.rewrite(query, op -> {
			Op a = TransformReplaceConstants.transform(op, x -> x.isURI() ? eval(isBnodeUriFnUri, NodeValue.makeNode(x)).getBoolean() : false);
			Op b = Transformer.transform(null, new ExprTransformVirtualBnodeUris(), a);
			Op c = ExprTransformVirtualBnodeUris.forceBnodeUris(b);
			return c;
		});

		return result;
	}
	
	
	public static void main(String[] args) {
		Expr input = ExprUtils.parse("?x = <bnode://foobar>");
//		Expr input = ExprUtils.parse("<bnode://foo> = <bnode://bar>");
		new ExprTransformVirtualBnodeUris();
		//Expr actual = ExprTransformer.transform(new ExprTransformBnodeDecode(), input);
		//System.out.println(actual);
		
//		Query query = QueryFactory.create("SELECT * { ?s a ?t . ?s ?p ?o }");
		Query query = QueryFactory.create("CONSTRUCT { ?s ?p ?o } { ?s <bnode://foobar> ?t . ?s ?p ?o . FILTER(?p = <bnode://foobar>)}");
		Query actual = rewrite(query);

		//		Op op = Algebra.compile(query);
//		op = forceBnodeUris(op);
//		Query actual = OpAsQuery.asQuery(op);
		System.out.println(actual);

	}


	public static Op forceBnodeUris(Op op) {
		List<Var> visibleVars = new ArrayList<>(OpVars.visibleVars(op));
		Set<Var> forbiddenVars = new HashSet<>(OpVars.mentionedVars(op));
		
		// Rename all visible vars
		Map<Var, Var> map = visibleVars.stream()
				.collect(Collectors.toMap(
						v -> v, v -> VarGeneratorBlacklist.create(v.getName(), forbiddenVars).next()));

		Op tmp = NodeTransformLib.transform(n -> n.isVariable() ? map.getOrDefault(n, (Var)n) : n, op);

		VarExprList vel = new VarExprList();
		for(Var v : visibleVars) {
			vel.add(v, subst(forceBnodeUriFnUri, new ExprVar(map.get(v))));
		}
		OpExtend result = OpExtend.create(tmp, vel);

		return result;
	}
	
}

