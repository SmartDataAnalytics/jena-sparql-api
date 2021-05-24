package org.aksw.jena_sparql_api.rdf.collections;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * NodeMapper for a specific given RDFDatatype
 * 
 * If the given datatype is null, canMap will always return false.
 * 
 * @author raven Apr 9, 2018
 *
 * @param <T>
 */
public class NodeMapperFromRdfDatatype<T>
	implements NodeMapper<T>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RDFDatatype dtype;
	
	public NodeMapperFromRdfDatatype(RDFDatatype dtype) {
		super();
		this.dtype = dtype;
	}
	
	@Override
	public boolean canMap(Node node) {
		// TODO we could make use of spring's conversion service to allow implicit conversions (e.g. int -> long)
		boolean result = dtype != null && canMapCore(node, dtype);
		return result;
	}

	/**
	 * Can the given node be converted to the given java class?
	 * 
	 * FIXME We need to integrate the coercion system from the mapper module:
	 * 
	 * @param node
	 * @param clazz
	 * @return
	 */
	public static boolean canMapCore(Node node, Class<?> clazz) {
		boolean result;
		
//		String lex = node.getLiteralLexicalForm();
//		RDFDatatype xdtype = node.getLiteralDatatype();
//		Object x = xdtype.parse(lex);

		
		Object obj = node.isLiteral() ? node.getLiteralValue() : null;
		Class<?> objClass = obj == null ? null : obj.getClass();

		if(objClass != null && clazz.isAssignableFrom(objClass)) {
			result = true;
		} else {
			TypeMapper tm = TypeMapper.getInstance();
			RDFDatatype dtype = tm.getTypeByClass(clazz);

			if(dtype == null){ 
				result = false;
				if(node.isLiteral()) {
					dtype = node.getLiteralDatatype();
					Function<Node, Object> c = getCoercion(dtype, clazz);
					result = c != null;
				}
			} else {
				result = canMapCore(node, dtype);
			}
			
		}
		
		return result;
	}

	// FIXME We need to integrate the coercion system from the mapper module:
	public static Function<Node, Object> getCoercion(RDFDatatype dtype, Class<?> tgtClazz) {
		Function<Node, Object> result = null;
		
		Class<?> srcClass = dtype.getJavaClass();
		if(srcClass != null) {
			if(XSDDateTime.class.isAssignableFrom(srcClass) && Calendar.class.isAssignableFrom(tgtClazz)) {
				result = node -> {
					Object obj = node.getLiteralValue();
					Calendar r = ((XSDDateTime)obj).asCalendar();
					return r;
				};
			}
		}
		
		return result;
	}

	public static boolean canMapCore(Node node, RDFDatatype dtype) {
		// TODO we could make use of spring's conversion service to allow implicit conversions (e.g. int -> long)
		boolean result;
		if(node.isLiteral()) {
			Object value = node.getLiteralValue();
			result = dtype.isValidValue(value);
		} else {
			result = false;
		}
		return result;		
	}
	
	@Override
	public Node toNode(T obj) {
		String lex = dtype.unparse(obj);
		Node result = NodeFactory.createLiteral(lex, dtype);
		return result;
	}

	@Override
	public T toJava(Node node) {
		T result = toJavaCore(node, dtype);
		return result;
	}

	public static <T> T toJavaCore(Node node, Class<?> clazz) {
		Object obj = node.isLiteral() ? node.getLiteralValue() : null;
		Class<?> objClass = obj == null ? null : obj.getClass();
		
		T result = null;
		if(objClass != null && clazz.isAssignableFrom(objClass)) {
			result = (T)obj;
		} else {
			TypeMapper tm = TypeMapper.getInstance();
			RDFDatatype dtype = tm.getTypeByClass(clazz);

			if(dtype == null) {
				if(node.isLiteral()) {
					if(dtype == null && node.isLiteral()) {
						dtype = node.getLiteralDatatype();
						Function<Node, Object> c = getCoercion(dtype, clazz);
						Object o = c.apply(node);
						result = (T)o;
					}
				}
				
				Objects.requireNonNull(dtype, "Expected an RDFDatatype for java class '" + clazz + "'");

			} else {
				result = toJavaCore(node, dtype);				
			}			
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T toJavaCore(Node node, RDFDatatype dtype) {
		Object obj;
		Class<?> javaClass = dtype.getJavaClass();
		
		/*
		 *  Unfortunately, Jena always narrows the type of numbers:
		 *  Even if e.g. the dtype.getJavaClass() reports a Long, dtype.parse() may yield Integer
		 *  
		 *  TODO Using reflection and parsing strings is pretty much the slowest approach to number conversion
		 */
		if(Number.class.isAssignableFrom(javaClass)) {
			String lex = node.getLiteralLexicalForm();
			if(javaClass.equals(BigDecimal.class)) {
				obj = new BigDecimal(lex);
			} else if(javaClass.equals(BigInteger.class)) {
				BigDecimal tmp = new BigDecimal(lex);
				obj = tmp.toBigInteger();//new BigInteger(lex);
			} else {
				Method m;
				try {
					try {
						m = javaClass.getMethod("valueOf", String.class);
					} catch(Exception e) {
						throw new RuntimeException("No 'valueOf' method found on Numeric for parsing " + node + " against java type " + javaClass + " based on RDF type " + dtype);
					}
					obj = m.invoke(null, lex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			obj = node.getLiteralValue();
		}

		return (T)obj;		
	}


	@Override
	public Class<?> getJavaClass() {
		Class<?> result = dtype.getJavaClass();
		return result;
	}
}