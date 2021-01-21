package org.aksw.jena_sparql_api.analytics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.mapper.parallel.AccLcaMap;
import org.aksw.jena_sparql_api.util.graph.alg.BreadthFirstSearchLib;
import org.aksw.jena_sparql_api.util.graph.alg.GraphSuccessorFunction;
import org.aksw.jena_sparql_api.util.graph.alg.NaiveLCAFinder;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.CastXSD;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Multiset;


public class LcaAccumulationTest {

	public static RDFDatatype pullUpType(String datatypeIri) {
		return pullUpType(NodeFactory.createURI(datatypeIri));
	}

	public static RDFDatatype pullUpType(Node datatype) {
		return pullUpType(Collections.singleton(datatype));
	}

	/**
	 * Given a set of starting types (wrapped as Nodes) and a backing type hierarchy,
	 * use breadth first search to find the first set of ancestor types which contain at least one
	 * type with a mapping to a Java datatypes w.r.t. Jena's {@link TypeMapper}.
	 * 
	 * @param start The set of string types
	 * @return
	 */
	public static RDFDatatype pullUpType(Set<Node> start) {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);

		TypeMapper tm = TypeMapper.getInstance();
		
		Stream<Set<Node>> breadthOfParentsStream = BreadthFirstSearchLib.stream(start, node -> gsf.apply(graph, node), Collectors::toSet);

		
		Map<Node, RDFDatatype> javaTypeMap = breadthOfParentsStream.map(set -> {
			Map<Node, RDFDatatype> map = set.stream()
				.map(node -> Maps.immutableEntry(node, tm.getTypeByName(node.getURI())))
				.filter(e -> e.getValue() != null && e.getValue().getJavaClass() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			return map;
		})
		.filter(map -> !map.isEmpty())
		.findFirst().orElse(null);

		
		RDFDatatype result;
		if (javaTypeMap == null) {
			result = null;
		} else {
			result = Iterables.getOnlyElement(javaTypeMap.values());
		}
		
		System.out.println("res: " + result);	
		return result;
	}
	
	@Test
	public void test() {
		Model model = RDFDataMgr.loadModel("xsd-ontology.ttl");		
		Graph graph = model.getGraph();
		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
		Set<Node> cappingTypes = Collections.singleton(NodeFactory.createURI(XSD.NS + "anyAtomicType"));
		// Set<Node> cappingTypes = Collections.emptySet();
		NaiveLCAFinder lcaFinder = new NaiveLCAFinder(graph, (n, g) -> gsf.apply(n, g).filter(m -> !cappingTypes.contains(m)));

		AccLcaMap<Node> acc = AccLcaMap.create(lcaFinder::getLCA);

		Node anyType = NodeFactory.createURI(XSD.NS + "anyType");
		
		Set<Node> sourceTypes = new LinkedHashSet<>(Arrays.asList(
				XSD.xdouble.asNode(),
				XSD.xint.asNode(),
				XSD.xshort.asNode(),
				XSD.decimal.asNode(),
				XSD.xlong.asNode(),
				XSD.xstring.asNode(),
				anyType));

		sourceTypes.forEach(acc::accumulate);

		Map<Node, Node> actual = acc.getValue();

		Map<Node, Node> expected = new HashMap<>();
		expected.put(XSD.xdouble.asNode(), XSD.xdouble.asNode());
		expected.put(XSD.xshort.asNode(), XSD.decimal.asNode());
		expected.put(XSD.xint.asNode(), XSD.decimal.asNode());
		expected.put(XSD.xlong.asNode(), XSD.decimal.asNode());
		expected.put(XSD.decimal.asNode(), XSD.decimal.asNode());
		expected.put(XSD.xstring.asNode(), XSD.xstring.asNode());
		expected.put(anyType, anyType);
				
		Assert.assertEquals(expected, actual);
		
		System.out.println(actual);
		Map<Node, RDFDatatype> targetTypes = actual.values().stream()
				.distinct()
				.map(node -> new SimpleEntry<>(node, pullUpType(node)))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		Map<Node, RDFDatatype> mapping = actual.entrySet().stream()
				.map(kv -> new SimpleEntry<>(kv.getKey(), targetTypes.get(kv.getValue())))
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				
		Set<Node> failedToMap = Sets.difference(actual.keySet(), mapping.keySet());

		
		// Map<Var, Multiset<String>> srcSchema = ResultSetAnalytics.usedDatatypes().asCollector();
		SchemaMapperImpl schemaMapper = new SchemaMapperImpl();
		// schemaMapper.createSchema(null, null)
		
		
		Function<Node, Object> fn = createNodeToJavaConverter(XSDDatatype.XSDlong, XSDDatatype.XSDbyte);
		Object r = fn.apply(NodeFactory.createLiteral("1", XSDDatatype.XSDlong));
		System.out.println(r + " - " + r.getClass());
//		System.out.println("Failed to map: " + failedToMap);
//		System.out.println(mapping);
//		
//		System.out.println(targetTypes);
	}
	
	
	
	/**
	 * 
	 * 
	 * @author raven
	 *
	 * @param <N> The name type. E.g. String or Var.
	 */
	interface FieldMapping<N> {
	
	}
	
	interface Field {
		
	}
	

	/**
	 * Create a lambda that converts from the given src datatype to the target one.
	 * 
	 * As a generic (performance-wise slow) fallback this will convert source nodes to the lexical form (string)
	 * and parse the string with the target datatype.
	 * 
	 * However, standard {@link Number} types are handled with short cuts that perform fast.
	 * 
	 * @param src
	 * @param tgt
	 * @return
	 */
	public static Function<Node, Object> createNodeToJavaConverter(RDFDatatype src, RDFDatatype tgt) {
		Function<Node, Object> result = null;

		Class<?> srcClass = src.getJavaClass();
		Class<?> tgtClass = tgt.getJavaClass();
		
		if (srcClass != null && tgtClass != null) {
			Function<Object, Object> javaConv = createNumberConverter(srcClass, tgtClass);
			if (javaConv != null) {
				result = node -> node == null ? null : javaConv.apply(node.getLiteralValue());
			}
		} else if (src instanceof XSDDatatype && tgt instanceof XSDDatatype) {
			result = node -> CastXSD.cast(NodeValue.makeNode(node), (XSDDatatype)tgt).asNode().getLiteralValue();
		}

		if (result == null) {
			// Fallback: parse the lexical form (will be very slow)
			result = node -> tgt.parse(node.getLiteralLexicalForm());
		}
		
		return result;
	}


	/**
	 * Dedicated converters for all standard java Number types (subclasses of Number)
	 * including BigInteger and BigDecimal.
	 * 
	 * Does not check for overflows.
	 * 
	 * Needs wrapping for null handling:
	 * <pre>
	 * Function<Object, Object> withoutNullHandling = createNumberConverter(..)
	 * Function<Object, Object> withNullHandling = arg -&gt; arg == null ? null : withoutNullHandling;
	 * </pre> 
	 * 
	 * Used for performance.
	 * 
	 * @param src
	 * @param tgt
	 * @return
	 */
	public static Function<Object, Object> createNumberConverter(Class<?> src, Class<?> tgt) {
		Function<Object, Object> result = null;
		
		if (Number.class.isAssignableFrom(src) && Number.class.isAssignableFrom(tgt)) {
			if (tgt.isAssignableFrom(src)) {
				// Tgt is the same a src or even a super class - no conversion needed 
				result = x -> x;
			} else if (Byte.class.equals(tgt)) {
				result = x -> ((Number)x).byteValue();
			} else if (Short.class.equals(tgt)) {
				result = x -> ((Number)x).shortValue();				
			} else if (Integer.class.equals(tgt)) {
				result = x -> ((Number)x).intValue();								
			} else if (Long.class.equals(tgt)) {
				result = x -> ((Number)x).longValue();				
			} else if (Float.class.equals(tgt)) {
				result = x -> ((Number)x).floatValue();			
			} else if (Double.class.equals(tgt)) {
				result = x -> ((Number)x).doubleValue();				
			} else if (BigInteger.class.equals(tgt)) {
//				if (isLongOrWeaker(src)) {
					result = x -> BigInteger.valueOf(((Number)x).longValue());
//				}
			} else if (BigDecimal.class.isAssignableFrom(tgt)) {
				if (isLongOrWeaker(src)) {
					result = x -> new BigDecimal(((Number)x).longValue());
				} else { // if (isDoubleOrWeaker(src)) {
					result = x -> new BigDecimal(((Number)x).doubleValue());					
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if the argument class is Long, Integer, Short or Byte
	 */
	public static boolean isLongOrWeaker(Class<?> cls) {
		return Long.class.isAssignableFrom(cls) ||
				Integer.class.isAssignableFrom(cls) ||
				Short.class.isAssignableFrom(cls) ||
				Byte.class.isAssignableFrom(cls);
	}

	/**
	 * Returns true if the argument class is Double or Float
	 */
	public static boolean isDoubleOrWeaker(Class<?> cls) {
		return Double.class.isAssignableFrom(cls) ||
				Float.class.isAssignableFrom(cls);
	}

}
