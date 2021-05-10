package org.aksw.jena_sparql_api.mapper.proxy.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aksw.commons.util.convert.Converter;
import org.aksw.commons.util.convert.ConverterRegistry;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.builders.ExprBuildException;

/**
 * Jena function implementation that delegates to a backing Java method.
 * 
 * Instances of this class are usually generated using the {@link FunctionGenerator}.
 * Use the {@link FunctionBinder} for creating function bindings and registration of them with Jena.
 * 
 * @author raven
 *
 */
public class FunctionAdapter
	implements Function
{
	protected Object invocationTarget;
	protected Method method;
	
	protected java.util.function.Function<Object, NodeValue> returnValueConverter;
	protected ConverterRegistry converterRegistry;
	protected TypeMapper typeMapper;
	
	protected Param[] params;
	

	protected int mandatoryArgsCount;
	
	public FunctionAdapter(
			Method method,
			Object invocationTarget,
			java.util.function.Function<Object, NodeValue> returnValueConverter,
			Param[] params,
			TypeMapper typeMapper,
			ConverterRegistry converterRegistry) {
		super();
		this.method = method;
		this.invocationTarget = invocationTarget;
		this.returnValueConverter = returnValueConverter;
		this.params = params;
		this.converterRegistry = converterRegistry;
		this.typeMapper = typeMapper;
	}

	@Override
	public void build(String uri, ExprList args) {
		
	}

//	protected Object exprToJava() {
//		register(GeometryWrapper.class, Geometry.class, GeometryWrapper::getParsingGeometry, g -> new GeometryWrapper(WKTDatatype.URI, g));
//	}
	
	
	public static Object convert(Object input, Class<?> target, ConverterRegistry converterRegistry) {
		Object result;
		
		if (input == null) {
			result = null;
		} else {
			Class<?> inputClass = input.getClass();
		
			if (org.apache.commons.lang3.ClassUtils.isAssignable(inputClass, target)) {
				result = input;
			} else {
				Converter converter = converterRegistry.getConverter(inputClass, target);
				
				if (converter == null) {
					throw new RuntimeException(String.format("No converter registered from %1$s to %2$s", inputClass, target));
				}
				
				result = converter.convert(input);
			}
		}
			
		return result;
	}
	
	@Override
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		Object[] javaArgs = new Object[params.length];
		
		int n = args.size();
		if (n < mandatoryArgsCount || n > params.length) {
			throw new ExprBuildException(
					String.format("at least %d and at most %d args expected but %d provided",
								mandatoryArgsCount, params.length, n));
		}
		
		for (int i = 0; i < params.length; ++i) {
			Object javaArg;
			Param param = params[i];
			Class<?> paramType = param.getParamClass();
			
			if (i < n) {
				Expr expr = args.get(i);

				// Attempt to convert the NodeValue's java object if its type does not match the
				// expected parameter type

//				if (Expr.NONE.equals(expr)) {
//					javaArg = null;
//				} else {
				NodeValue arg = expr.eval(binding, env);

				NodeValue nv = arg.getConstant();
				Node node = nv.asNode();
				Object intermediateObj = node.getLiteralValue();
				
//				try {
					javaArg = convert(intermediateObj, paramType, converterRegistry);
//				} catch (Exception e) {
//					throw new ExprEvalException(e);
//				}
//				else {
//					throw new IllegalArgumentException("Constant expression expected, got: " + expr);
//				}
			} else {
				javaArg = param.defaultValue;
			}			
			
			javaArgs[i] = javaArg;
		}
		
		Object val;
		try {
			val = method.invoke(invocationTarget, javaArgs);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ExprEvalException(e);
		}
		
		NodeValue result = returnValueConverter.apply(val);
		return result;
//		
//		Object targetReturnVal = resultValueConverter == null
//				? val
//				: resultValueConverter.getFunction().apply(val);
//
//		Node node = NodeFactory.createLiteralByValue(val, resultRdfDatatype);
//		NodeValue nv = NodeValue.makeNode(node);
//		
//		Class<?> returnType = method.getReturnType();
//		
//		RDFDatatype dtype = typeMapper.getTypeByValue(val);
//		if (dtype == null) {
//			Class<?> targetReturnType = returnTypeConversionMap.get(returnType);
//			
//			if (targetReturnType == null) {
//				throw new RuntimeException(String.format("Could not find a mapping from %s to an RDF datatype", returnType));
//			}
//			
//			dtype = typeMapper.getTypeByClass(targetReturnType);
//			
//			
//			Converter converter = converterRegistry.getConverter(returnType, targetReturnType)
//			if (converter == null) {
//				throw new RuntimeException(String.format("No converter found from %s to %s", returnType, targetReturnType));
//			}
//					
//			Object obj = converter.getFunction().apply(val);
//			Node node = NodeFactory.createLiteralByValue(obj, dtype);
//			NodeValue nv = NodeValue.makeNode(node);
//			
//		}
//		
//		
//		
//		// TODO Auto-generated method stub
//		return null;
	}
	
	
}