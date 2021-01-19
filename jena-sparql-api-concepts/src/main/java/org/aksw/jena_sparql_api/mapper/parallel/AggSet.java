package org.aksw.jena_sparql_api.mapper.parallel;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Supplier;

import org.aksw.jena_sparql_api.mapper.Accumulator;
import org.aksw.jena_sparql_api.mapper.parallel.AggBuilder.SerializableSupplier;

public class AggSet<I, C extends Set<I>>
	implements ParallelAggregator<I, C, Accumulator<I, C>>,
	Serializable
{
	private static final long serialVersionUID = 8383392928490694149L;

	protected Supplier<? extends C> collectionSupplier;
	
	public AggSet(SerializableSupplier<? extends C> newCollection) {
		super();
		this.collectionSupplier = newCollection;
	}

//	public static AggSet<I, C extends Set<I>> create(Class<I> inputType, Class<C> setType, SerializableSupplier<? extends C> newCollection) {
//	
//	}


	@Override
	public Accumulator<I, C> createAccumulator() {
		return new AccSet(collectionSupplier.get());
	}

	@Override
	public Accumulator<I, C> combine(Accumulator<I, C> a, Accumulator<I, C> b) {
		C ca = a.getValue();
		C cb = b.getValue();
		
		Accumulator<I, C>  result;
		if (ca.size() > cb.size()) {
			ca.addAll(cb);
			result = a;
		} else {
			cb.addAll(ca);
			result = b;
		}
		
		return result;
	}

	public class AccSet
		implements Accumulator<I, C>, Serializable
	{
		private static final long serialVersionUID = -377712930606295862L;
		protected C value;
		
		public AccSet(C value) {
			super();
			this.value = value;
		}

		@Override
		public void accumulate(I item) {
			value.add(item);
		}
	
		@Override
		public C getValue() {
			return value;
		}
	}
}

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


