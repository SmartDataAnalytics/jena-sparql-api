package org.aksw.jena_sparql_api.transform.result_set;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionDecorator;
import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.aksw.jena_sparql_api.utils.DatasetGraphUtils;
import org.aksw.jena_sparql_api.utils.ExtendedIteratorClosable;
import org.aksw.jena_sparql_api.utils.IteratorResultSetBinding;
import org.aksw.jena_sparql_api.utils.NodeTransformLib2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
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

    @Override
    public ResultSet execSelect() {
        ResultSet core = super.execSelect();

        ResultSet result = applyNodeTransform(nodeTransform, core);
        return result;
    }

    @Override
    public Model execConstruct() {
        Model model = super.execConstruct();
        Model result = NodeTransformLib2.copyWithNodeTransform(nodeTransform, model);
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
        Dataset result = NodeTransformLib2.applyNodeTransform(nodeTransform, dataset);
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
        Model result = NodeTransformLib2.copyWithNodeTransform(nodeTransform, model);
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

	public static ResultSet applyNodeTransform(NodeTransform nodeTransform, ResultSet rs) {
	    Closeable closeable = rs instanceof Closeable ? (Closeable)rs : null;
	    List<String> vars = rs.getResultVars();
	
	    ExtendedIterator<Binding> it = WrappedIterator.create(new IteratorResultSetBinding(rs))
	        .mapWith(b -> NodeTransformLib2.transformValues(b, nodeTransform));
	
	    QueryIter queryIter = new QueryIterPlainWrapper(it);
	    ResultSet core = ResultSetFactory.create(queryIter, vars);
	
	    ResultSet result = new ResultSetCloseable(core, closeable);
	    return result;
	}

    
    public static void main(String[] args) {
        Model model = ModelFactory.createDefaultModel();
        Node bn = NodeFactory.createBlankNode("test");
        model.add(model.wrapAsResource(bn), RDF.type, RDF.Property);

        NodeTransform nodeTransform = NodeTransformLib2.createBnodeLabelTransform(ExprUtils.parse("CONCAT('_:', ?x )"), Vars.x);

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
