package org.aksw.jena_sparql_api.conjure.dataset.algebra;

// Use OpData with an empty model
//@ResourceView
//@RdfTypeNs("rpif")
//public interface OpNothing
//	extends Op0
//{
//	@Override
//	default <T> T accept(OpVisitor<T> visitor) {
//		T result = visitor.visit(this);
//		return result;
//	}
//	
////	public static OpVar create(String name) {
////		OpVar result = create(ModelFactory.createDefaultModel(), name);
////
////		return result;
////	}
//
//	public static OpNothing create(Model model, String name) {
//		OpNothing result = model
//				.createResource().as(OpNothing.class);
//
//		return result;
//	}
//}
