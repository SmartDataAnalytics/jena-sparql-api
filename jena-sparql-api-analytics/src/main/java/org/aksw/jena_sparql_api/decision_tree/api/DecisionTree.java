package org.aksw.jena_sparql_api.decision_tree.api;

import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;





//class SparqlInnerNode<T>
//
//{	
//}






/**
 * Decision tree where leafs carry sets of expressions
 * This is de-facto a convenience representation where expressions in the leaf nodes
 * implicitly have BOUND(?var) conditions for each variable mentioned in the expressions.
 * 
 * For this reason, this class may become deprecated.
 * 
 */
//class DecisionTreeSparqlExprSet
//	extends DecisionTreeSparqlBase<Set<Expr>>
//{
//	private static final long serialVersionUID = 5760959713235187882L;
//
//	public Node eval(Binding binding) {
//		Collection<LeafNode<Expr, Node, Set<Expr>>> leafs = findLeafNodes(binding);
//		
//		Node result;
//		if (leafs.isEmpty()) {
//			result = null;
//		} else {
//			LeafNode<Expr, Node, Set<Expr>> leaf = Iterables.getOnlyElement(leafs);
//			Set<Expr> exprs = leaf.getValue();
//			
////			Set<Node> vals = new HashSet<>();
//			result = null;
//			for (Expr expr : exprs) {
//				NodeValue nv = ExprUtils.eval(expr, binding);
//				Node node = nv == null ? null : nv.asNode();
//				
//				if (node != null) {
//					result = node;
//					break;
//				}
//			}
//		}
//		
//		return result;
//	}
//}



/**
 *  
 * 
 * @author raven
 *
 * @param <I>
 * @param <T>
 */
public class DecisionTree {// <I, C, T, N extends DtNode<C, T>> {

	public static void main(String[] args) {
		DecisionTreeSparqlExpr dt = new DecisionTreeSparqlExpr();
		
		InnerNode<Expr, Node, Expr> dn = dt.getRoot().getOrCreateInnerNode(null, ExprUtils.parse("?p = 'test'"));
		
		
		InnerNode<Expr, Node, Expr> on = dn.getOrCreateInnerNode(NodeValue.TRUE.asNode(), ExprUtils.parse("?o = 'hello'"));
		on.getOrCreateLeafNode(NodeValue.TRUE.asNode()).setValue(ExprUtils.parse("'oho'"));
		
		
//		LeafNode<Expr, Node, Expr> yes = dn.getOrCreateLeafNode(NodeValue.TRUE.asNode());
//		yes.setValue(ExprUtils.parse("'yay'"));

		LeafNode<Expr, Node, Expr> no = dn.getOrCreateLeafNode(NodeValue.FALSE.asNode());
		no.setValue(ExprUtils.parse("'nah'"));

		LeafNode<Expr, Node, Expr> otherwise = dn.getOrCreateLeafNode(null);
		otherwise.setValue(ExprUtils.parse("'fail'"));

		
		Node result1 = dt.eval(BindingFactory.binding(Vars.p, NodeFactory.createLiteral("test")));
		System.out.println(result1);

		Node result2 = dt.eval(BindingFactory.binding(Vars.o, NodeFactory.createLiteral("test")));
		System.out.println(result2);
		

		BindingMap bm = BindingFactory.create();
		bm.add(Vars.p, NodeFactory.createLiteral("test"));
		bm.add(Vars.o, NodeFactory.createLiteral("hello"));
		Node result3 = dt.eval(bm);
		System.out.println(result3);


	}
	// Stream<> find(I input);
}
