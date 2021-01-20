package org.aksw.jena_sparql_api.mapper.parallel;

/**
 * Create a collecting aggregator from e.g. any for of collection supplier, such as
 * Lists, Sets or Multisets.
 * 
 * Actually, the AccCollection can be used with AggNatural so an AggCollection is not needed.
 * 
 * 
 * 
 * @author raven
 *
 * @param <I>
 * @param <C>
 */
//public class AggCollection<I, C extends Collection<I>>
//	implements ParallelAggregator<I, C, Accumulator<I, C>>,
//	Serializable
//{
//	private static final long serialVersionUID = 8383392928490694149L;
//
//	protected Supplier<? extends C> collectionSupplier;
//	
//	public AggCollection(SerializableSupplier<? extends C> newCollection) {
//		super();
//		this.collectionSupplier = newCollection;
//	}
//
////	public static AggSet<I, C extends Set<I>> create(Class<I> inputType, Class<C> setType, SerializableSupplier<? extends C> newCollection) {
////	
////	}
//
//
//	@Override
//	public Accumulator<I, C> createAccumulator() {
//		return new AccCollection(collectionSupplier.get());
//	}
//
//	@Override
//	public Accumulator<I, C> combine(Accumulator<I, C> a, Accumulator<I, C> b) {
//		C ca = a.getValue();
//		C cb = b.getValue();
//		
//		Accumulator<I, C>  result;
//		if (ca.size() > cb.size()) {
//			ca.addAll(cb);
//			result = a;
//		} else {
//			cb.addAll(ca);
//			result = b;
//		}
//		
//		return result;
//	}
//
//}

//	
//	public static void create() {
//		
//		Graph graph = model.getGraph();
//		GraphSuccessorFunction gsf = GraphSuccessorFunction.create(RDFS.subClassOf.asNode(), true);
//		
//		LeastCommonAncestor alg = new LeastCommonAncestor(graph, gsf);
//		
//		// Find any types that are lcas of the given ones
//		Set<Node> actual = alg.leastCommonAncestors(XSD.nonNegativeInteger.asNode(), XSD.decimal.asNode());
//		Set<Node> expected = Collections.singleton(XSD.decimal.asNode());
//
//		// Traverse the type hierarchy up until we find a type that has a corresponding java class
//		TypeMapper tm = TypeMapper.getInstance();
//		
//		Stream<Set<Node>> breadthStream = BreadthFirstSearchLib.stream(expected, node -> gsf.apply(graph, node), Collectors::toSet);
//		
////		Node type = expected.iterator().next();
////		RDFDatatype dtype = tm.getTypeByName(type.getURI());
//
//		// Find the first breadth for which a java type is found
//		Map<Node, RDFDatatype> javaTypeMap = breadthStream.map(set -> {
//			Map<Node, RDFDatatype> map = set.stream()
//				.map(node -> Maps.immutableEntry(node, tm.getTypeByName(node.getURI())))
//				.filter(e -> e.getValue() != null && e.getValue().getJavaClass() != null)
//				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//			return map;
//		})
//		.filter(map -> !map.isEmpty())
//		.findFirst().orElse(null);
//
//		System.out.println(javaTypeMap);
//		System.out.println(actual);
//	}


