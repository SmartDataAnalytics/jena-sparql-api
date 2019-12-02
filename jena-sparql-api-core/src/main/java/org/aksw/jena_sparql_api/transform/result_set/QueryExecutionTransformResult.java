package org.aksw.jena_sparql_api.transform.result_set;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;


public class QueryExecutionTransformResult
	extends QueryExecutionDecorator
{
	//protected Converter<Node, Node> nodeConverter;
	protected NodeTransform nodeTransform;
	
	public QueryExecutionTransformResult(QueryExecution decoratee, NodeTransform nodeTransform) {
		super(decoratee);
		this.nodeTransform = nodeTransform;
	}
	
	
//    public static Table transformValue(Table table, NodeTransform transform) {
//        // Non-streaming rewrite 
//        List<Var> vars = transformVars(transform, table.getVars()) ;
//        Iterator<Binding> iter = table.rows() ; 
//        List<Binding> newRows = new ArrayList<>() ;
//        for ( ; iter.hasNext() ; ) { 
//            Binding b = iter.next() ;
//            Binding b2 = transform(b, transform) ;
//            newRows.add(b2) ;
//        }
//        return new TableData(vars, newRows) ;
//    }
    
    public static Binding transformValues(Binding b, NodeTransform transform) {
        BindingMap b2 = BindingFactory.create() ;
        List<Var> vars = Iter.toList(b.vars()) ;
        for (Var v : vars) {
        	Node before = b.get(v);
        	Node after = transform.apply(before);
            b2.add(v, after);
        }
        return b2;
    }


	public static ResultSet applyNodeTransform(NodeTransform nodeTransform, ResultSet rs) {
		Closeable closeable = rs instanceof Closeable ? (Closeable)rs : null;
		List<String> vars = rs.getResultVars();

		ExtendedIterator<Binding> it = WrappedIterator.create(new IteratorResultSetBinding(rs))
			.mapWith(b -> transformValues(b, nodeTransform));

		QueryIter queryIter = new QueryIterPlainWrapper(it);
		ResultSet core = ResultSetFactory.create(queryIter, vars);

		ResultSet result = new ResultSetCloseable(core, closeable);
		return result;
	}
	
	public static Graph applyNodeTransform(NodeTransform nodeTransform, Graph graph) {
		Graph result = GraphFactory.createDefaultGraph();
		graph.find().mapWith(t -> NodeTransformLib.transform(nodeTransform, t))
			.forEachRemaining(result::add);
		return result;
	}

	public static RDFNode applyNodeTransform(NodeTransform nodeTransform, RDFNode rdfNode) {
		Model beforeModel = rdfNode.getModel();
		Model afterModel = applyNodeTransform(nodeTransform, beforeModel);
		Node beforeNode = rdfNode.asNode();
		Node tmp = nodeTransform.apply(beforeNode);
		Node afterNode = tmp == null ? beforeNode : tmp;
		RDFNode result = afterModel.asRDFNode(afterNode);
		
		return result;
	}

	public static Model applyNodeTransform(NodeTransform nodeTransform, Model model) {
		Graph oldGraph = model.getGraph();
		Graph newGraph = applyNodeTransform(nodeTransform, oldGraph);
		Model result = ModelFactory.createModelForGraph(newGraph);
		return result;
	}
	
	public static DatasetGraph applyNodeTransform(NodeTransform nodeTransform, DatasetGraph dg) {
		DatasetGraph result = DatasetGraphFactory.create();
		WrappedIterator.create(dg.find())
			.mapWith(q -> NodeTransformLib.transform(nodeTransform, q))
			.forEachRemaining(result::add);
		return result;
	}

	public static Dataset applyNodeTransform(NodeTransform nodeTransform, Dataset dataset) {
		DatasetGraph oldDg = dataset.asDatasetGraph();
		DatasetGraph newDg = applyNodeTransform(nodeTransform, oldDg);
		Dataset result = DatasetFactory.wrap(newDg);
		return result;
	}

	public static <T> ExtendedIterator<T> map(Iterator<T> it, Function<? super T, ? extends T> xform) {
		ExtendedIterator<T> result = WrappedIterator.create(it).mapWith(x -> xform.apply(x));
		return result;
	}
	
	@Override
	public ResultSet execSelect() {
		ResultSet core = super.execSelect();

		ResultSet result = applyNodeTransform(nodeTransform, core);
		return result;
	}

	@Override
	public Model execConstruct() {
		Model model = super.execConstruct();
		Model result = applyNodeTransform(nodeTransform, model);
		return result;
	}
	
	@Override
	public Model execConstruct(Model model) {
		Model tmp = execConstruct();
		model.add(tmp);
		return model;
	}

	@Override
	public Iterator<Triple> execConstructTriples() {
		Iterator<Triple> core = super.execConstructTriples();
		Iterator<Triple> result = ExtendedIteratorClosable.create(core, this)
				.mapWith(t -> NodeTransformLib.transform(nodeTransform, t));

		return result;
	}
	
	@Override
	public Dataset execConstructDataset() {
		Dataset dataset = super.execConstructDataset();
		Dataset result = applyNodeTransform(nodeTransform, dataset);
		return result;
	}

	@Override
	public Dataset execConstructDataset(Dataset dataset) {
		Dataset tmp = execConstructDataset();
		DatasetGraphUtils.addAll(dataset.asDatasetGraph(), tmp.asDatasetGraph());
		return dataset;
	}

	@Override
	public Model execDescribe() {
		Model model = super.execConstruct();
		Model result = applyNodeTransform(nodeTransform, model);
		return result;
	}
	
	@Override
	public Model execDescribe(Model model) {
		Model tmp = execDescribe();
		model.add(tmp);
		return model;
	}
	
	@Override
	public Iterator<Triple> execDescribeTriples() {
		Iterator<Triple> core = super.execDescribeTriples();
		Iterator<Triple> result = ExtendedIteratorClosable.create(core, this)
				.mapWith(t -> NodeTransformLib.transform(nodeTransform, t));

		return result;
	}

	/**
	 * Create a conditional expression checks whether its argument is a blank node,
	 * and if so, extracts its bnode label and forwards it to 'bnodeLabelTransform'.
	 * 
	 * IF(isBlank(?x), bnodeLabelTransform(afn:bnode(?x)), ?x)
	 * 
	 * @param bnodeLabelTransform
	 * @param v
	 * @return
	 */
	public static Expr addBnodeCheckAndTransform(Expr bnodeLabelTransform, Var v) {
		ExprVar ev = new ExprVar(v);
		Expr bnodeLabelOf = new E_Function("http://jena.apache.org/ARQ/function#bnode", new ExprList(ev));
		Expr e2 = ExprTransformer.transform(new ExprTransformSubstitute(v, bnodeLabelOf), bnodeLabelTransform);
		Expr result = new E_Conditional(new E_IsBlank(ev), new E_IRI(e2), ev);
		return result;
	}

	/**
	 * Create a node transform from an expression that maps bnode label strings
	 * to strings that can be used as IRIS in a query.
	 * 
	 * An example for such an expression is CONCAT('_:', ?x )
	 * 
	 * @param bnodeLabelTransform
	 * @param v
	 * @return
	 */
	public static NodeTransform createBnodeLabelTransform(Expr bnodeLabelTransform, Var v) {
		Expr conditionalExpr = addBnodeCheckAndTransform(bnodeLabelTransform, v);
		NodeTransform result = createNodeTransform(conditionalExpr, v);
		return result;
	}

	/**
	 * Create a node transformer from an expression
	 * 
	 * @param expr
	 * @param v
	 * @return
	 */
	public static NodeTransform createNodeTransform(Expr expr, Var v) {
		return x -> {
			Binding b = BindingFactory.binding(v, x);
			NodeValue nv = ExprUtils.eval(expr, b);
			Node r = nv.asNode();
			return r;
		};
		
//		return x -> {
//			Node r;
//			if(x.isBlank()) {
//				String str = x.getBlankNodeLabel();
//				Node node = NodeFactory.createLiteral(str);
//				Binding b = BindingFactory.binding(v, node);
//				NodeValue nv = ExprUtils.eval(expr, b);
//				r = nv.asNode();
//			} else {
//				r = x;
//			}
//			return r;
//		}
	}
	
	public static void main(String[] args) {
		Model model = ModelFactory.createDefaultModel();
		Node bn = NodeFactory.createBlankNode("test");
		model.add(model.wrapAsResource(bn), RDF.type, RDF.Property);
		
		NodeTransform nodeTransform = createBnodeLabelTransform(ExprUtils.parse("CONCAT('_:', ?x )"), Vars.x);
		
//		NodeTransform nodeTransform = x -> {
//			Node r = x.equals(RDF.Nodes.Property) ? OWL.ObjectProperty.asNode() : x;
//			System.out.println(x + " -> " + r);
//			return r;
//		};
		
		try(QueryExecution qe = new QueryExecutionTransformResult(
				QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", model), nodeTransform)) {

			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
		}
	}
}
